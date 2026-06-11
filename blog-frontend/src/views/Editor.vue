<template>
  <div class="editor">
    <el-card>
      <el-input v-model="form.title" placeholder="文章标题" size="large" style="margin-bottom:16px" />
      <div style="margin-bottom:12px;display:flex;gap:10px;align-items:center">
        <el-button @click="$refs.fileInput.click()" :loading="uploading">上传封面图</el-button>
        <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleFileChange" />
        <el-image v-if="form.coverImage" :src="form.coverImage" style="width:200px;height:120px;border-radius:4px" fit="cover" />
        <el-button v-if="form.coverImage" text type="danger" @click="form.coverImage=''">移除封面</el-button>
      </div>
      <el-input v-model="form.content" type="textarea" :rows="15" placeholder="支持 Markdown 语法..." />
      <div style="margin-top:16px;display:flex;gap:10px">
        <el-button type="primary" @click="save(1)" :loading="saving">发布</el-button>
        <el-button @click="save(0)" :loading="saving">保存草稿</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const uploading = ref(false)
const form = ref({ title: '', content: '', coverImage: '' })

onMounted(() => {
  if (route.params.id) {
    request.get(`/posts/${route.params.id}`).then(res => {
      form.value = { title: res.title, content: res.content, coverImage: res.coverImage || '' }
    })
  }
})

function handleFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  uploading.value = true
  const fd = new FormData()
  fd.append('file', file)
  request.post('/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } }).then(res => {
    form.value.coverImage = res.url
  }).finally(() => uploading.value = false)
}

function save(status) {
  if (!form.value.title.trim()) { ElMessage.warning('请输入标题'); return }
  saving.value = true
  const data = { ...form.value, status }
  const req = route.params.id
    ? request.put(`/posts/${route.params.id}`, data)
    : request.post('/posts', data)
  req.then(() => {
    ElMessage.success(status === 1 ? '发布成功' : '已保存草稿')
    router.push('/')
  }).finally(() => saving.value = false)
}
</script>

<style scoped>
.editor { max-width: 900px; margin: 0 auto; }
</style>
