<template>
  <div class="login-page">
    <!-- 装饰背景 -->
    <div class="bg-decoration">
      <div class="circle c1"></div>
      <div class="circle c2"></div>
      <div class="circle c3"></div>
    </div>

    <div class="login-wrapper">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-icon">🎓</div>
          <h1>校园知乎</h1>
          <p>Campus Q&A Platform</p>
          <div class="brand-features">
            <div class="feature-item">
              <span class="dot"></span> 智能问答社区
            </div>
            <div class="feature-item">
              <span class="dot"></span> 校园知识共享
            </div>
            <div class="feature-item">
              <span class="dot"></span> 高效内容管理
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录区 -->
      <div class="form-section">
        <div class="form-inner">
          <h2>管理后台</h2>
          <p class="subtitle">欢迎回来，请登录您的账号</p>

          <a-form :model="form" layout="vertical" @finish="handleLogin" style="margin-top: 32px">
            <a-form-item name="username" :rules="[{ required: true, message: '请输入账号' }]">
              <a-input v-model:value="form.username" placeholder="管理员账号" size="large" class="custom-input">
                <template #prefix><UserOutlined style="color: #bbb" /></template>
              </a-input>
            </a-form-item>
            <a-form-item name="password" :rules="[{ required: true, message: '请输入密码' }]">
              <a-input-password v-model:value="form.password" placeholder="密码" size="large" class="custom-input">
                <template #prefix><LockOutlined style="color: #bbb" /></template>
              </a-input-password>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" html-type="submit" :loading="loading" block size="large" class="login-btn">
                登 录
              </a-button>
            </a-form-item>
          </a-form>

          <div class="login-hint">
            <SafetyCertificateOutlined /> 默认管理员: admin / admin123
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, SafetyCertificateOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const loading = ref(false)
const form = ref({ username: '', password: '' })

const handleLogin = async () => {
  loading.value = true
  try {
    const data = await login(form.value)
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_username', data.user.username)
    message.success('登录成功')
    router.push('/dashboard')
  } catch (e) {}
  finally { loading.value = false }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f0f5ff;
  position: relative;
  overflow: hidden;
}

/* 背景装饰圆 */
.bg-decoration { position: absolute; inset: 0; pointer-events: none; }
.circle {
  position: absolute; border-radius: 50%; opacity: 0.08;
}
.c1 { width: 600px; height: 600px; background: #1677ff; top: -200px; right: -100px; }
.c2 { width: 400px; height: 400px; background: #36cfc9; bottom: -100px; left: -80px; }
.c3 { width: 200px; height: 200px; background: #597ef7; top: 40%; left: 10%; }

.login-wrapper {
  display: flex;
  width: 880px;
  min-height: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(22, 119, 255, 0.1), 0 1px 3px rgba(0,0,0,0.04);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* 左侧品牌区 */
.brand-section {
  width: 380px;
  background: linear-gradient(160deg, #1677ff 0%, #0958d9 50%, #003eb3 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  position: relative;
}
.brand-section::after {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}
.brand-content {
  color: #fff;
  text-align: center;
  position: relative;
  z-index: 1;
}
.brand-icon {
  font-size: 64px;
  margin-bottom: 16px;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.2));
}
.brand-content h1 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 4px;
  letter-spacing: 2px;
}
.brand-content > p {
  font-size: 14px;
  opacity: 0.7;
  letter-spacing: 1px;
}
.brand-features {
  margin-top: 40px;
  text-align: left;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  opacity: 0.85;
  margin-bottom: 14px;
}
.dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: #69c0ff; flex-shrink: 0;
}

/* 右侧表单区 */
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 56px;
}
.form-inner {
  width: 100%;
  max-width: 340px;
}
.form-inner h2 {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 4px;
}
.subtitle {
  color: #8c8c8c;
  font-size: 14px;
}

.custom-input {
  border-radius: 8px;
  height: 48px;
}
.custom-input :deep(.ant-input) {
  height: 48px;
  font-size: 15px;
}

.login-btn {
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  border: none;
  box-shadow: 0 4px 16px rgba(22, 119, 255, 0.3);
  transition: all 0.3s;
}
.login-btn:hover {
  box-shadow: 0 6px 24px rgba(22, 119, 255, 0.45);
  transform: translateY(-1px);
}

.login-hint {
  text-align: center;
  color: #bfbfbf;
  font-size: 12px;
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

/* 响应式 */
@media (max-width: 768px) {
  .login-wrapper { flex-direction: column; width: 90%; min-height: auto; }
  .brand-section { width: 100%; padding: 32px; min-height: 200px; }
  .form-section { padding: 32px 24px; }
}
</style>
