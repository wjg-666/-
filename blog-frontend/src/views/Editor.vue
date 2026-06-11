<template>
  <div class="editor-page">
    <div class="editor-toolbar">
      <el-input
        v-model="form.title"
        placeholder="文章标题"
        size="large"
        class="title-input"
        maxlength="200"
        show-word-limit
      />
      <div class="meta-row">
        <el-select v-model="form.categoryId" placeholder="选择分类" clearable style="width:180px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-input
          v-model="tagInput"
          placeholder="输入标签，按回车添加"
          style="width:300px"
          @keyup.enter="addTag"
          @keyup.delete="tagInput === '' && removeLastTag()"
        >
          <template #prefix>
            <el-tag
              v-for="(tag, idx) in form.tagNames"
              :key="tag"
              closable
              size="small"
              @close="removeTag(idx)"
              style="margin-right:4px"
            >
              {{ tag }}
            </el-tag>
          </template>
        </el-input>
      </div>
      <div class="toolbar-actions">
        <div class="cover-section">
          <el-button @click="fileInput.click()" :loading="uploading" :icon="Picture">
            {{ form.coverImage ? '更换封面' : '上传封面' }}
          </el-button>
          <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleFileChange" />
          <el-image
            v-if="form.coverImage"
            :src="form.coverImage"
            class="cover-preview"
            fit="cover"
          />
          <el-button v-if="form.coverImage" text type="danger" size="small" @click="form.coverImage=''">
            移除
          </el-button>
        </div>
        <div class="publish-actions">
          <el-button @click="save(0)" :loading="saving">保存草稿</el-button>
          <el-button type="primary" @click="save(1)" :loading="saving">发布</el-button>
        </div>
      </div>
    </div>

    <div class="editor-container">
      <div class="editor-pane">
        <div class="pane-header">
          <el-icon><EditPen /></el-icon>
          <span>Markdown 编辑</span>
        </div>
        <textarea
          v-model="form.content"
          class="editor-textarea"
          placeholder="开始用 Markdown 写作吧...

# 一级标题
## 二级标题
**粗体** *斜体* `代码`

- 列表项
1. 有序列表

> 引用文字

```代码块```"
        ></textarea>
      </div>
      <div class="preview-pane">
        <div class="pane-header">
          <el-icon><View /></el-icon>
          <span>实时预览</span>
        </div>
        <div class="preview-content" v-html="renderedContent"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import { Picture, EditPen, View } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const fileInput = ref(null)
const saving = ref(false)
const uploading = ref(false)
const categories = ref([])
const tagInput = ref('')
const form = ref({ title: '', content: '', coverImage: '', categoryId: null, tagNames: [] })
const isEdit = ref(false)

const renderedContent = computed(() => {
  if (!form.value.content) return '<p style="color:#999">在左侧输入内容即可预览...</p>'
  return marked(form.value.content || '')
})

onMounted(() => {
  fetchCategories()
  if (route.params.id) {
    isEdit.value = true
    request.get(`/posts/${route.params.id}`).then(res => {
      const post = res.data || res
      form.value = {
        title: post.title || '',
        content: post.content || '',
        coverImage: post.coverImage || '',
        categoryId: post.categoryId || null,
        tagNames: post.tagNames || []
      }
    })
  }
})

function fetchCategories() {
  request.get('/categories').then(res => { categories.value = res || [] })
}

function addTag() {
  const name = tagInput.value.trim()
  if (!name) return
  if (form.value.tagNames.includes(name)) {
    ElMessage.warning('标签已存在')
    return
  }
  form.value.tagNames.push(name)
  tagInput.value = ''
}

function removeTag(idx) {
  form.value.tagNames.splice(idx, 1)
}

function removeLastTag() {
  if (form.value.tagNames.length > 0) {
    tagInput.value = form.value.tagNames.pop()
  }
}

function handleFileChange(e) {
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
    form.value.coverImage = res.url || res.data?.url || res
  }).finally(() => uploading.value = false)
}

function save(status) {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  saving.value = true
  const data = { ...form.value, status }
  const req = isEdit.value
    ? request.put(`/posts/${route.params.id}`, data)
    : request.post('/posts', data)
  req.then(() => {
    ElMessage.success(status === 1 ? '发布成功！' : '草稿已保存')
    router.push('/')
  }).finally(() => saving.value = false)
}
</script>

<style scoped>
.editor-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 16px;
}

.editor-toolbar {
  margin-bottom: 16px;
}

.title-input {
  margin-bottom: 12px;
}

.meta-row {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.cover-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cover-preview {
  width: 120px;
  height: 68px;
  border-radius: 6px;
}

.editor-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  height: calc(100vh - 250px);
  min-height: 500px;
}

.editor-pane, .preview-pane {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.pane-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

.editor-textarea {
  flex: 1;
  border: none;
  padding: 16px;
  font-size: 15px;
  line-height: 1.8;
  font-family: 'Cascadia Code', 'Fira Code', 'Consolas', monospace;
  resize: none;
  outline: none;
  color: #303133;
}

.editor-textarea::placeholder {
  color: #c0c4cc;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.preview-content {
  flex: 1;
  padding: 16px 24px;
  overflow-y: auto;
  line-height: 1.8;
  color: #303133;
}

.preview-content :deep(h1) { font-size: 2em; margin: 0.5em 0; border-bottom: 1px solid #eee; padding-bottom: 0.3em; }
.preview-content :deep(h2) { font-size: 1.6em; margin: 0.5em 0; }
.preview-content :deep(h3) { font-size: 1.3em; margin: 0.4em 0; }
.preview-content :deep(p) { margin: 0.8em 0; }
.preview-content :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding: 4px 16px;
  margin: 12px 0;
  color: #666;
  background: #f5f7fa;
}
.preview-content :deep(pre) {
  background: #f6f8fa;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
  font-size: 14px;
}
.preview-content :deep(code) {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Cascadia Code', 'Fira Code', monospace;
  font-size: 0.9em;
}
.preview-content :deep(pre code) {
  background: none;
  padding: 0;
}
.preview-content :deep(img) {
  max-width: 100%;
  border-radius: 6px;
}
.preview-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
}
.preview-content :deep(th), .preview-content :deep(td) {
  border: 1px solid #e4e7ed;
  padding: 8px 12px;
  text-align: left;
}
.preview-content :deep(th) {
  background: #f5f7fa;
}

/* Mobile: stack vertically */
@media (max-width: 768px) {
  .editor-container {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr 1fr;
    height: auto;
  }
  .editor-pane, .preview-pane {
    min-height: 350px;
  }
}
</style>
