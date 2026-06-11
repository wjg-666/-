<template>
  <div class="auth-container">
    <div class="auth-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    <div class="auth-card">
      <div class="card-left">
        <div class="brand">
          <div class="brand-icon">✨</div>
          <h1>加入我们</h1>
          <p>创建你的专属博客空间</p>
        </div>
        <div class="illustration">
          <svg viewBox="0 0 200 160" xmlns="http://www.w3.org/2000/svg">
            <circle cx="100" cy="55" r="22" fill="#fff" opacity="0.15"/>
            <rect x="75" y="30" width="50" height="50" rx="25" fill="#fff" opacity="0.1"/>
            <rect x="30" y="90" width="140" height="50" rx="8" fill="#fff" opacity="0.1"/>
            <rect x="50" y="100" width="100" height="6" rx="3" fill="#fff" opacity="0.15"/>
            <rect x="60" y="114" width="80" height="6" rx="3" fill="#fff" opacity="0.15"/>
            <rect x="70" y="128" width="60" height="6" rx="3" fill="#fff" opacity="0.12"/>
          </svg>
        </div>
      </div>
      <div class="card-right">
        <h2>创建账号</h2>
        <p class="subtitle">注册成为博主，开始你的创作之旅</p>
        <el-form :model="form" @submit.prevent="handleRegister" class="auth-form">
          <div class="input-wrapper">
            <span class="input-icon">👤</span>
            <input v-model="form.username" placeholder="用户名（至少3位）" class="styled-input" />
          </div>
          <div class="input-wrapper">
            <span class="input-icon">✏️</span>
            <input v-model="form.nickname" placeholder="昵称（可选）" class="styled-input" />
          </div>
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input v-model="form.password" type="password" placeholder="密码（至少6位）" class="styled-input" @keyup.enter="handleRegister" />
          </div>
          <button type="submit" class="auth-btn" :disabled="loading">
            <span v-if="loading" class="spinner"></span>
            <span v-else>注 册</span>
          </button>
        </el-form>
        <p class="switch-link">已有账号？<router-link to="/login">立即登录 →</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const form = ref({ username: '', nickname: '', password: '' })

function handleRegister() {
  if (form.value.username.length < 3) { ElMessage.warning('用户名至少3位'); return }
  if (form.value.password.length < 6) { ElMessage.warning('密码至少6位'); return }
  loading.value = true
  request.post('/auth/register', form.value).then(() => {
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  }).finally(() => loading.value = false)
}
</script>

<style scoped>
.auth-container {
  min-height: calc(100vh - 140px);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.auth-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.06;
}
.shape-1 {
  width: 450px; height: 450px;
  background: #67c23a;
  top: -180px; left: -120px;
}
.shape-2 {
  width: 300px; height: 300px;
  background: #409eff;
  bottom: -80px; right: -80px;
}
.shape-3 {
  width: 180px; height: 180px;
  background: #e6a23c;
  top: 40%; right: 15%;
}
.auth-card {
  display: flex;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.08), 0 0 0 1px rgba(0,0,0,0.04);
  overflow: hidden;
  width: 780px;
  min-height: 480px;
}
.card-left {
  width: 340px;
  background: linear-gradient(135deg, #67c23a 0%, #4a9e2f 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #fff;
  text-align: center;
}
.brand-icon {
  font-size: 48px;
  margin-bottom: 16px;
}
.brand h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: 2px;
}
.brand p {
  font-size: 14px;
  opacity: 0.8;
  margin: 0;
}
.illustration {
  margin-top: 36px;
  width: 180px;
}
.card-right {
  flex: 1;
  padding: 48px 44px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.card-right h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 6px;
}
.subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0 0 32px;
}
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.input-wrapper {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 10px;
  padding: 0 16px;
  transition: box-shadow 0.2s;
}
.input-wrapper:focus-within {
  box-shadow: 0 0 0 2px rgba(103,194,58,0.2);
}
.input-icon {
  font-size: 16px;
  margin-right: 10px;
  opacity: 0.5;
}
.styled-input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 14px 0;
  font-size: 15px;
  color: #303133;
  outline: none;
}
.styled-input::placeholder {
  color: #c0c4cc;
}
.auth-btn {
  margin-top: 8px;
  padding: 14px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #67c23a, #4a9e2f);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.2s;
  letter-spacing: 4px;
}
.auth-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(103,194,58,0.35);
}
.auth-btn:active {
  transform: translateY(0);
}
.auth-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.spinner {
  display: inline-block;
  width: 20px; height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.switch-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #909399;
}
.switch-link a {
  color: #67c23a;
  text-decoration: none;
  font-weight: 500;
}
.switch-link a:hover {
  text-decoration: underline;
}

@media (max-width: 800px) {
  .auth-card {
    flex-direction: column;
    width: 92vw;
    min-height: auto;
  }
  .card-left {
    width: 100%;
    padding: 30px 24px;
  }
  .illustration { display: none; }
  .card-right {
    padding: 32px 24px;
  }
}
</style>
