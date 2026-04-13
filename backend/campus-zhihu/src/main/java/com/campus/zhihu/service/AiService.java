package com.campus.zhihu.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.zhihu.entity.Question;
import com.campus.zhihu.entity.SystemConfig;
import com.campus.zhihu.mapper.QuestionMapper;
import com.campus.zhihu.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final SystemConfigMapper configMapper;
    private final QuestionMapper questionMapper;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 流式调用 AI 回答问题
     */
    public SseEmitter streamChat(Long questionId) {
        SseEmitter emitter = new SseEmitter(180_000L);
        final boolean[] completed = {false};

        // 客户端断开时标记已完成，避免继续发送
        emitter.onCompletion(() -> completed[0] = true);
        emitter.onTimeout(() -> completed[0] = true);
        emitter.onError(e -> completed[0] = true);

        executor.submit(() -> {
            try {
                String apiKey = getConfigValue("ai_api_key");
                String model = getConfigValue("ai_model");
                String baseUrl = getConfigValue("ai_base_url");
                String systemPrompt = getConfigValue("ai_system_prompt");

                if (apiKey == null || apiKey.isEmpty()) {
                    emitter.send(SseEmitter.event().data("{\"error\":\"AI 功能未配置，请联系管理员\"}"));
                    emitter.complete();
                    return;
                }

                Question q = questionMapper.selectById(questionId);
                if (q == null) {
                    emitter.send(SseEmitter.event().data("{\"error\":\"问题不存在\"}"));
                    emitter.complete();
                    return;
                }

                // 构建请求体
                JSONObject body = new JSONObject();
                body.set("model", model != null ? model : "deepseek-ai/DeepSeek-V2.5");
                body.set("stream", true);

                JSONArray messages = new JSONArray();
                if (systemPrompt != null && !systemPrompt.isEmpty()) {
                    messages.add(new JSONObject().set("role", "system").set("content", systemPrompt));
                }
                String userContent = "问题标题：" + q.getTitle() + "\n\n问题详情：" + q.getContent();
                messages.add(new JSONObject().set("role", "user").set("content", userContent));
                body.set("messages", messages);

                // 发起 HTTP 请求
                String apiUrl = (baseUrl != null ? baseUrl : "https://api.siliconflow.cn/v1") + "/chat/completions";
                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "text/event-stream");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(180000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    BufferedReader errReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                    StringBuilder errBuilder = new StringBuilder();
                    String errLine;
                    while ((errLine = errReader.readLine()) != null) errBuilder.append(errLine);
                    errReader.close();
                    log.error("AI API 调用失败: {} - {}", responseCode, errBuilder);
                    emitter.send(SseEmitter.event().data("{\"error\":\"AI 服务调用失败，请稍后重试\"}"));
                    emitter.complete();
                    return;
                }

                // 读取流式响应
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (completed[0]) {
                            log.info("客户端已断开，停止读取 AI 响应");
                            conn.disconnect();
                            break;
                        }
                        if (line.startsWith("data:")) {
                            String data = line.substring(5).trim();
                            if ("[DONE]".equals(data)) {
                                if (!completed[0]) emitter.send(SseEmitter.event().data("[DONE]"));
                                break;
                            }
                            if (data.isEmpty()) continue;

                            try {
                                JSONObject json = JSONUtil.parseObj(data);
                                JSONArray choices = json.getJSONArray("choices");
                                if (choices != null && !choices.isEmpty()) {
                                    JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                                    if (delta != null && delta.containsKey("content")) {
                                        String content = delta.getStr("content");
                                        if (content != null && !content.isEmpty()) {
                                            if (!completed[0]) {
                                                emitter.send(SseEmitter.event().data(content));
                                            }
                                        }
                                    }
                                }
                            } catch (Exception parseEx) {
                                if (!completed[0]) {
                                    log.warn("解析 AI 响应数据异常: {}", parseEx.getMessage());
                                }
                            }
                        }
                    }
                }

                if (!completed[0]) emitter.complete();
            } catch (Exception e) {
                if (!completed[0]) {
                    log.error("AI 流式回答异常", e);
                    try {
                        emitter.send(SseEmitter.event().data("{\"error\":\"AI 回答异常：" + e.getMessage() + "\"}"));
                    } catch (Exception ignored) {
                    }
                }
                if (!completed[0]) emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 测试 AI 连接 - 发送一个简短请求验证 API Key 和服务是否可用
     * @return null 表示成功，否则返回错误信息
     */
    public String testConnection(String apiKey, String model, String baseUrl) {
        try {
            JSONObject body = new JSONObject();
            body.set("model", model);
            body.set("stream", false);
            body.set("max_tokens", 5);

            JSONArray messages = new JSONArray();
            messages.add(new JSONObject().set("role", "user").set("content", "hi"));
            body.set("messages", messages);

            String apiUrl = baseUrl + "/chat/completions";
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes("UTF-8"));
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                return null; // 成功
            }
            // 读取错误信息
            BufferedReader errReader = new BufferedReader(new InputStreamReader(
                    conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = errReader.readLine()) != null) sb.append(line);
            errReader.close();

            if (code == 401) {
                return "API Key 无效，请检查后重试";
            } else if (code == 403) {
                return "API Key 权限不足";
            } else {
                return "HTTP " + code + " - " + sb;
            }
        } catch (java.net.SocketTimeoutException e) {
            return "连接超时，请检查 API 地址是否正确";
        } catch (java.net.UnknownHostException e) {
            return "无法解析域名，请检查 API 地址";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * 从数据库获取配置值
     */
    public String getConfigValue(String key) {
        SystemConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 更新配置值
     */
    public void setConfigValue(String key, String value) {
        SystemConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        if (config != null) {
            config.setConfigValue(value);
            configMapper.updateById(config);
        }
    }
}
