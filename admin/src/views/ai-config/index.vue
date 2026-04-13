<template>
  <div>
    <div class="page-header"><h2>AI 配置</h2></div>
    <a-spin :spinning="loading">
      <a-card title="硅基流动 API 配置" style="max-width: 720px">
        <template #extra>
          <a-tag :color="form.ai_api_key ? 'green' : 'red'">{{ form.ai_api_key ? '已配置' : '未配置' }}</a-tag>
        </template>
        <a-form :label-col="{ span: 4 }">
          <a-form-item label="API Key" required>
            <a-input-password v-model:value="form.ai_api_key" placeholder="输入硅基流动 API Key" />
            <div style="margin-top: 4px; color: #999; font-size: 12px">
              在 <a href="https://cloud.siliconflow.cn" target="_blank">硅基流动控制台</a> 获取 API Key
            </div>
          </a-form-item>
          <a-form-item label="模型" required>
            <a-select v-model:value="form.ai_model" placeholder="选择 AI 模型" showSearch allowClear>
              <a-select-option value="deepseek-ai/DeepSeek-V2.5">DeepSeek-V2.5</a-select-option>
              <a-select-option value="deepseek-ai/DeepSeek-V3">DeepSeek-V3</a-select-option>
              <a-select-option value="Pro/deepseek-ai/DeepSeek-V3">DeepSeek-V3 (Pro)</a-select-option>
              <a-select-option value="deepseek-ai/DeepSeek-R1">DeepSeek-R1</a-select-option>
              <a-select-option value="Pro/deepseek-ai/DeepSeek-R1">DeepSeek-R1 (Pro)</a-select-option>
              <a-select-option value="Qwen/Qwen2.5-7B-Instruct">Qwen2.5-7B</a-select-option>
              <a-select-option value="Qwen/Qwen2.5-72B-Instruct">Qwen2.5-72B</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="API 地址">
            <a-input v-model:value="form.ai_base_url" placeholder="https://api.siliconflow.cn/v1" />
          </a-form-item>
          <a-form-item label="系统提示词">
            <a-textarea v-model:value="form.ai_system_prompt" :rows="4" placeholder="设置 AI 的角色和行为" />
          </a-form-item>
          <a-form-item :wrapper-col="{ offset: 4 }">
            <a-button type="primary" @click="save" :loading="saving">保存配置</a-button>
            <a-button style="margin-left: 12px" @click="testConnection" :loading="testing">
              测试连接
            </a-button>
          </a-form-item>
        </a-form>
      </a-card>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAiConfig, updateAiConfig, testAiConfig } from '../../api'
import { message } from 'ant-design-vue'

const loading = ref(false), saving = ref(false), testing = ref(false)
const form = reactive({
  ai_api_key: '',
  ai_model: 'deepseek-ai/DeepSeek-V2.5',
  ai_base_url: 'https://api.siliconflow.cn/v1',
  ai_system_prompt: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await getAiConfig()
    Object.keys(data).forEach(k => { if (form.hasOwnProperty(k)) form[k] = data[k] || '' })
  } catch (e) {}
  finally { loading.value = false }
}

const save = async () => {
  if (!form.ai_api_key) { message.error('请输入 API Key'); return }
  if (!form.ai_model) { message.error('请选择模型'); return }
  saving.value = true
  try {
    await updateAiConfig({ ...form })
    message.success('配置已保存')
    loadData()
  } catch (e) { message.error('保存失败') }
  finally { saving.value = false }
}

const testConnection = async () => {
  if (!form.ai_api_key) { message.error('请先输入 API Key'); return }
  if (!form.ai_model) { message.error('请先选择模型'); return }
  testing.value = true
  try {
    await updateAiConfig({ ...form })
    const res = await testAiConfig({ ...form })
    message.success(res || '连接测试通过')
  } catch (e) {
    // 错误已由响应拦截器处理，无需重复提示
  }
  finally { testing.value = false }
}

onMounted(loadData)
</script>
