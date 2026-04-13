package com.campus.zhihu.controller;

import com.campus.zhihu.common.Result;
import com.campus.zhihu.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * 用户端 - AI 流式回答问题
     */
    @GetMapping(value = "/api/ai/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestParam Long questionId) {
        return aiService.streamChat(questionId);
    }

    /**
     * 管理端 - 获取 AI 配置
     */
    @GetMapping("/api/admin/ai-config")
    public Result<?> getAiConfig() {
        Map<String, String> result = new HashMap<>();
        String[] keys = {"ai_api_key", "ai_model", "ai_base_url", "ai_system_prompt"};
        for (String key : keys) {
            String value = aiService.getConfigValue(key);
            if ("ai_api_key".equals(key) && value != null && value.length() > 8) {
                result.put(key, value.substring(0, 4) + "****" + value.substring(value.length() - 4));
            } else {
                result.put(key, value != null ? value : "");
            }
        }
        return Result.success(result);
    }

    /**
     * 管理端 - 测试 AI 连接
     */
    @PostMapping("/api/admin/ai-config/test")
    public Result<?> testAiConnection(@RequestBody Map<String, String> configs) {
        String apiKey = configs.get("ai_api_key");
        String model = configs.get("ai_model");
        String baseUrl = configs.get("ai_base_url");

        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("****")) {
            // 如果前端传的是脱敏 key，用数据库里的
            String dbKey = aiService.getConfigValue("ai_api_key");
            if (dbKey == null || dbKey.isEmpty()) {
                return Result.error("请先输入有效的 API Key");
            }
            apiKey = dbKey;
        }
        if (model == null || model.isEmpty()) {
            model = "deepseek-ai/DeepSeek-V2.5";
        }
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "https://api.siliconflow.cn/v1";
        }

        String errorMsg = aiService.testConnection(apiKey, model, baseUrl);
        if (errorMsg == null) {
            return Result.success("连接测试通过，AI 服务可用");
        } else {
            return Result.error("连接测试失败：" + errorMsg);
        }
    }

    /**
     * 管理端 - 更新 AI 配置
     */
    @PutMapping("/api/admin/ai-config")
    public Result<?> updateAiConfig(@RequestBody Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 如果 API Key 是脱敏的，跳过更新
            if ("ai_api_key".equals(key) && value != null && value.contains("****")) {
                continue;
            }
            aiService.setConfigValue(key, value);
        }
        return Result.success();
    }
}
