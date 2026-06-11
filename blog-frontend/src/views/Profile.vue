<template>
  <div class="profile">
    <el-card>
      <template #header><h3>个人信息</h3></template>
      <el-descriptions v-if="userStore.user" :column="2" border>
        <el-descriptions-item label="用户名">{{ userStore.user.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userStore.user.nickname }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ userStore.user.role === 0 ? '管理员' : '博主' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDate(userStore.user.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top:20px">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <h3 style="margin:0">我的文章</h3>
          <el-button type="primary" size="small" @click="$router.push('/editor')">写文章</el-button>
        </div>
      </template>
      <el-table :data="myPosts" stripe>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="viewCount" label="阅读" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{row}"><el-tag :type="row.status===1?'success':'info'">{{ row.status===1?'已发布':'草稿' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180"><template #default="{row}">{{ formatDate(row.createTime) }}</template></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{row}">
            <el-button size="small" @click="$router.push(`/editor/${row.id}`)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const myPosts = ref([])

function formatDate(d) { return d ? d.replace('T', ' ').substring(0, 19) : '' }

function fetchMyPosts() {
  request.get('/posts', { params: { page: 1, size: 100, keyword: '' } }).then(res => {
    // Filter posts by current user
    myPosts.value = (res.records || []).filter(p => p.authorId === userStore.user?.id)
  })
}

function handleDelete(id) {
  ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }).then(() => {
    request.delete(`/posts/${id}`).then(() => fetchMyPosts())
  }).catch(() => {})
}

onMounted(fetchMyPosts)
</script>

<style scoped>
.profile { max-width: 900px; margin: 0 auto; }
</style>
