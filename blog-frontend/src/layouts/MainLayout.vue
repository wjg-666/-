<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo" @click="$router.push('/')">📝 轻博客</div>
      <div class="header-right">
        <el-input v-model="keyword" placeholder="搜索文章..." size="default" style="width: 200px" @keyup.enter="search">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button v-if="!userStore.user" @click="$router.push('/login')">登录</el-button>
        <el-button v-if="!userStore.user" @click="$router.push('/register')">注册</el-button>
        <el-dropdown v-if="userStore.user" style="margin-left:12px">
          <span class="user-info">
            <el-avatar :size="32" :src="userStore.user.avatar" />
            <span>{{ userStore.user.nickname }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/editor')">写文章</el-dropdown-item>
              <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item v-if="userStore.user.role === 0" @click="$router.push('/admin')">管理后台</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main><router-view /></el-main>
    <el-footer class="footer">© 2026 轻博客 - Lightweight Blog</el-footer>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')

function search() {
  router.push({ path: '/', query: { keyword: keyword.value } })
}

function handleLogout() {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped>
.layout { min-height: 100vh; }
.header { display: flex; align-items: center; justify-content: space-between; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.1); position: sticky; top: 0; z-index: 100; }
.logo { font-size: 20px; font-weight: bold; cursor: pointer; color: #409eff; }
.header-right { display: flex; align-items: center; gap: 10px; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.footer { text-align: center; color: #999; padding: 20px; }
</style>
