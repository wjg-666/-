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
          <div class="brand-icon">📝</div>
          <h1>轻博客</h1>
          <p>记录思考，分享知识</p>
        </div>
        <div class="illustration">
          <svg viewBox="0 0 200 160" xmlns="http://www.w3.org/2000/svg">
            <rect x="40" y="20" width="120" height="90" rx="6" fill="#fff" opacity="0.15"/>
            <rect x="55" y="35" width="90" height="6" rx="3" fill="#fff" opacity="0.2"/>
            <rect x="55" y="48" width="70" height="4" rx="2" fill="#fff" opacity="0.15"/>
            <rect x="55" y="58" width="80" height="4" rx="2" fill="#fff" opacity="0.15"/>
            <circle cx="165" cy="45" r="16" fill="#fff" opacity="0.12"/>
            <rect x="40" y="115" width="120" height="25" rx="6" fill="#fff" opacity="0.1"/>
            <rect x="70" y="122" width="60" height="10" rx="3" fill="#fff" opacity="0.15"/>
          </svg>
        </div>
      </div>
      <div class="card-right">
        <h2>欢迎回来</h2>
        <p class="subtitle">登录你的账户继续创作</p>
        <el-form :model="form" @submit.prevent="handleLogin" class="auth-form">
          <div class="input-wrapper">
            <span class="input-icon">👤</span>
            <input v-model="form.username" placeholder="用户名" class="styled-input" autocomplete="username" />
          </div>
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input v-model="form.password" type="password" placeholder="密码" class="styled-input" autocomplete="current-password" @keyup.enter="handleLogin" />
          </div>
          <button type="submit" class="auth-btn" :disabled="loading">
            <span v-if="loading" class="spinner"></span>
            <span v-else>登 录</span>
          </button>
        </el-form>
        <p class="switch-link">还没有账号？<router-link to="/register">立即注册 →</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = ref({ username: '', password: '' })

function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  userStore.login(form.value).then(() => {
    ElMessage.success('登录成功')
    router.push('/')
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
  width: 500px; height: 500px;
  background: #409eff;
  top: -200px; right: -150px;
}
.shape-2 {
  width: 350px; height: 350px;
  background: #67c23a;
  bottom: -100px; left: -100px;
}
.shape-3 {
  width: 200px; height: 200px;
  background: #e6a23c;
  top: 50%; left: 10%;
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
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
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
  box-shadow: 0 0 0 2px rgba(64,158,255,0.2);
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
  background: linear-gradient(135deg, #409eff, #337ecc);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.2s;
  letter-spacing: 4px;
}
.auth-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(64,158,255,0.35);
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
  color: #409eff;
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
