<template>
  <div class="profile">
    <!-- 个人信息卡片 -->
    <el-card>
      <template #header>
        <div class="card-header-row">
          <h3>个人信息</h3>
          <el-button v-if="!editing" type="primary" size="small" @click="startEdit">编辑</el-button>
          <div v-else class="edit-actions">
            <el-button size="small" @click="cancelEdit">取消</el-button>
            <el-button size="small" type="primary" @click="saveProfile" :loading="saving">保存</el-button>
          </div>
        </div>
      </template>

      <el-descriptions v-if="!editing && userStore.user" :column="2" border>
        <el-descriptions-item label="用户名">{{ userStore.user.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userStore.user.nickname }}</el-descriptions-item>
        <el-descriptions-item label="头像">
          <el-avatar :size="40" :src="userStore.user.avatar" />
        </el-descriptions-item>
        <el-descriptions-item label="角色">{{ userStore.user.role === 0 ? '管理员' : '博主' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDate(userStore.user.createTime) }}</el-descriptions-item>
      </el-descriptions>

      <!-- 编辑模式 -->
      <el-form v-else :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" maxlength="50" />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-edit">
            <el-avatar :size="64" :src="editForm.avatar" />
            <el-button size="small" @click="$refs.avatarInput.click()" :loading="uploading">上传头像</el-button>
            <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="handleAvatarUpload" />
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 我的文章 -->
    <el-card style="margin-top:20px">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <h3 style="margin:0">我的文章</h3>
          <el-button type="primary" size="small" @click="$router.push('/editor')">写文章</el-button>
        </div>
      </template>
      <el-table :data="myPosts" stripe v-loading="postLoading">
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{row}">
            <router-link :to="`/post/${row.id}`" class="post-link">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.status===1?'success':'info'">{{ row.status===1?'已发布':'草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="{row}">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{row}">
            <el-button size="small" @click="$router.push(`/editor/${row.id}`)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!postLoading && myPosts.length === 0" description="还没有文章，去写一篇吧！" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { setUserInfo } from '@/utils/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const myPosts = ref([])
const postLoading = ref(false)
const editing = ref(false)
const saving = ref(false)
const uploading = ref(false)
const editForm = ref({ nickname: '', avatar: '' })

function formatDate(d) {
  if (!d) return ''
  if (typeof d === 'string') return d.replace('T', ' ').substring(0, 19)
  return ''
}

function fetchMyPosts() {
  postLoading.value = true
  request.get('/posts', { params: { page: 1, size: 100 } }).then(res => {
    const records = res.records || res.data?.records || []
    myPosts.value = records.filter(p => p.authorId === userStore.user?.id)
  }).finally(() => postLoading.value = false)
}

function startEdit() {
  editForm.value = {
    nickname: userStore.user?.nickname || '',
    avatar: userStore.user?.avatar || ''
  }
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

function handleAvatarUpload(e) {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过5MB')
    return
  }
  uploading.value = true
  const fd = new FormData()
  fd.append('file', file)
  request.post('/upload', fd).then(res => {
    editForm.value.avatar = res.url || res
  }).finally(() => uploading.value = false)
}

async function saveProfile() {
  saving.value = true
  try {
    const res = await request.put('/auth/profile', editForm.value)
    const userData = res.data || res
    // Update store and localStorage
    const updatedUser = { ...userStore.user, ...userData }
    userStore.user = updatedUser
    setUserInfo(updatedUser)
    editing.value = false
    ElMessage.success('个人信息已更新')
  } catch (err) {
    // Error handled by interceptor
  } finally {
    saving.value = false
  }
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
.card-header-row { display: flex; justify-content: space-between; align-items: center; }
.edit-actions { display: flex; gap: 8px; }
.avatar-edit { display: flex; align-items: center; gap: 16px; }
.post-link { color: #303133; text-decoration: none; font-weight: 500; }
.post-link:hover { color: #409eff; }
</style>
