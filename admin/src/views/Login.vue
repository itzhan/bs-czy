<template>
  <div style="display:flex;justify-content:center;align-items:center;height:100vh;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);">
    <el-card style="width:400px;" shadow="always">
      <h2 style="text-align:center;margin-bottom:24px;color:#303133;">校园知乎 - 管理后台</h2>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="管理员账号" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width:100%;" size="large">登 录</el-button>
        </el-form-item>
      </el-form>
      <div style="text-align:center;color:#909399;font-size:12px;">默认管理员: admin / admin123</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const form = ref({ username: '', password: '' })

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) return ElMessage.warning('请输入账号和密码')
  loading.value = true
  try {
    const data = await login(form.value)
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_username', data.user.username)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>
