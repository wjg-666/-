<template>
  <div class="admin-dashboard">
    <!-- 顶部统计卡片 -->
    <div class="stats-row">
      <div class="stat-card users">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-label">用户总数</div>
        </div>
      </div>
      <div class="stat-card posts">
        <div class="stat-icon">📄</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.postCount }}</div>
          <div class="stat-label">文章总数</div>
        </div>
      </div>
      <div class="stat-card comments">
        <div class="stat-icon">💬</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.commentCount }}</div>
          <div class="stat-label">评论总数</div>
        </div>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="tabs">
      <button :class="['tab', { active: activeTab === 'posts' }]" @click="activeTab = 'posts'">
        📰 文章管理
      </button>
      <button :class="['tab', { active: activeTab === 'users' }]" @click="activeTab = 'users'">
        👤 用户管理
      </button>
      <button :class="['tab', { active: activeTab === 'comments' }]" @click="activeTab = 'comments'; fetchComments()">
        💬 评论管理
      </button>
      <button :class="['tab', { active: activeTab === 'categories' }]" @click="activeTab = 'categories'; fetchCategories()">
        📂 分类管理
      </button>
      <button :class="['tab', { active: activeTab === 'tags' }]" @click="activeTab = 'tags'; fetchTags()">
        🏷️ 标签管理
      </button>
    </div>

    <!-- 文章管理 -->
    <div v-if="activeTab === 'posts'" class="panel">
      <div class="panel-header">
        <h3>文章列表</h3>
        <div class="panel-actions">
          <el-input v-model="postKeyword" placeholder="搜索标题..." size="default" style="width:200px" clearable @input="fetchPosts" />
          <el-select v-model="postUserId" placeholder="按用户筛选" clearable style="width:180px" @change="fetchPosts">
            <el-option v-for="u in users" :key="u.id" :label="u.nickname || u.username" :value="u.id" />
          </el-select>
        </div>
      </div>
      <el-table :data="posts" stripe style="width:100%" v-loading="postLoading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="180">
          <template #default="{ row }">
            <router-link :to="`/post/${row.id}`" class="post-link">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column label="作者" width="120">
          <template #default="{ row }">
            <div class="author-cell">
              <el-avatar :size="24" :src="row.authorAvatar">{{ (row.authorName || '?')[0] }}</el-avatar>
              <span>{{ row.authorName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读" width="70" />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDeletePost(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="postTotal > postSize"
        v-model:current-page="postPage"
        :total="postTotal"
        :page-size="postSize"
        layout="prev, pager, next"
        @current-change="fetchPosts"
        style="justify-content:center;margin-top:20px"
      />
    </div>

    <!-- 用户管理 -->
    <div v-if="activeTab === 'users'" class="panel">
      <div class="panel-header">
        <h3>用户列表</h3>
        <el-button size="default" type="primary" @click="showUserPosts(null)">查看全部文章</el-button>
      </div>
      <el-table :data="users" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column label="角色" width="80">
          <template #default="{ row }">
            <el-tag :type="row.role === 0 ? 'danger' : 'primary'" size="small">
              {{ row.role === 0 ? '管理员' : '博主' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showUserPosts(row)">查看文章</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 评论管理 -->
    <div v-if="activeTab === 'comments'" class="panel">
      <div class="panel-header">
        <h3>评论列表</h3>
      </div>
      <el-table :data="comments" stripe style="width:100%" v-loading="commentLoading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="评论者" width="130">
          <template #default="{ row }">
            {{ row.username || row.guestName || '匿名' }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="postId" label="文章ID" width="80" />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定删除此评论？" @confirm="handleDeleteComment(row)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="commentTotal > commentSize"
        v-model:current-page="commentPage"
        :total="commentTotal"
        :page-size="commentSize"
        layout="prev, pager, next"
        @current-change="fetchComments"
        style="justify-content:center;margin-top:20px"
      />
    </div>

    <!-- 分类管理 -->
    <div v-if="activeTab === 'categories'" class="panel">
      <div class="panel-header">
        <h3>分类列表</h3>
        <el-button size="default" type="primary" @click="showCategoryDialog()">新增分类</el-button>
      </div>
      <el-table :data="categories" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="200" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="showCategoryDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除此分类？" @confirm="handleDeleteCategory(row)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 标签管理 -->
    <div v-if="activeTab === 'tags'" class="panel">
      <div class="panel-header">
        <h3>标签列表</h3>
        <el-button size="default" type="primary" @click="showTagDialog()">新增标签</el-button>
      </div>
      <el-table :data="tags" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="标签名称" min-width="200" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="showTagDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除此标签？" @confirm="handleDeleteTag(row)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分类弹窗 -->
    <el-dialog v-model="categoryDialogVisible" :title="editingCategory.id ? '编辑分类' : '新增分类'" width="400px">
      <el-input v-model="categoryForm.name" placeholder="分类名称" maxlength="50" />
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory" :loading="categorySaving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 标签弹窗 -->
    <el-dialog v-model="tagDialogVisible" :title="editingTag.id ? '编辑标签' : '新增标签'" width="400px">
      <el-input v-model="tagForm.name" placeholder="标签名称" maxlength="50" />
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTag" :loading="tagSaving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 用户文章弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="750px" destroy-on-close>
      <el-table :data="userPosts" stripe max-height="400" v-loading="userPostLoading">
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/post/${row.id}`" class="post-link">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读" width="70" />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDeleteUserPost(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!userPostLoading && userPosts.length === 0" description="暂无文章" :image-size="60" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

// --- 统计数据 ---
const stats = ref({ userCount: 0, postCount: 0, commentCount: 0 })

// --- Tab ---
const activeTab = ref('posts')

// --- 用户列表 ---
const users = ref([])

// --- 文章管理 ---
const posts = ref([])
const postPage = ref(1)
const postSize = ref(10)
const postTotal = ref(0)
const postKeyword = ref('')
const postUserId = ref(null)
const postLoading = ref(false)

// --- 评论管理 ---
const comments = ref([])
const commentPage = ref(1)
const commentSize = ref(10)
const commentTotal = ref(0)
const commentLoading = ref(false)

// --- 用户文章弹窗 ---
const dialogVisible = ref(false)
const dialogTitle = ref('')
const userPosts = ref([])
const userPostLoading = ref(false)

function formatDate(d) {
  return d ? d.replace('T', ' ').substring(0, 19) : ''
}

function fetchStats() {
  request.get('/admin/stats').then(res => stats.value = res)
}

function fetchUsers() {
  request.get('/admin/users', { params: { page: 1, size: 200 } }).then(res => {
    users.value = res.records || []
  })
}

function fetchPosts() {
  postLoading.value = true
  request.get('/admin/posts', {
    params: {
      page: postPage.value,
      size: postSize.value,
      keyword: postKeyword.value || undefined,
      userId: postUserId.value || undefined
    }
  }).then(res => {
    posts.value = res.records || []
    postTotal.value = res.total || 0
  }).finally(() => postLoading.value = false)
}

function fetchComments() {
  commentLoading.value = true
  request.get('/admin/comments', {
    params: { page: commentPage.value, size: commentSize.value }
  }).then(res => {
    comments.value = res.records || []
    commentTotal.value = res.total || 0
  }).finally(() => commentLoading.value = false)
}

function handleDeletePost(row) {
  ElMessageBox.confirm(`确定删除文章「${row.title}」？`, '确认删除', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  }).then(() => {
    request.delete(`/admin/posts/${row.id}`).then(() => {
      ElMessage.success('已删除')
      fetchPosts()
      fetchStats()
    })
  }).catch(() => {})
}

function handleDeleteComment(row) {
  request.delete(`/admin/comments/${row.id}`).then(() => {
    ElMessage.success('评论已删除')
    comments.value = comments.value.filter(c => c.id !== row.id)
    commentTotal.value--
    fetchStats()
  })
}

function toggleStatus(row) {
  request.put(`/admin/users/${row.id}/status`).then(() => {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.success('状态已更新')
  })
}

function showUserPosts(user) {
  if (user) {
    dialogTitle.value = `${user.nickname || user.username} 的文章`
    userPostLoading.value = true
    dialogVisible.value = true
    request.get('/admin/posts', { params: { userId: user.id, size: 200 } }).then(res => {
      userPosts.value = res.records || []
    }).finally(() => userPostLoading.value = false)
  } else {
    activeTab.value = 'posts'
    postUserId.value = null
    fetchPosts()
  }
}

// --- 分类管理 ---
const categories = ref([])
const categoryDialogVisible = ref(false)
const categorySaving = ref(false)
const editingCategory = ref({})
const categoryForm = ref({ name: '' })

function fetchCategories() {
  request.get('/categories').then(res => { categories.value = res || [] })
}

function showCategoryDialog(row) {
  editingCategory.value = row || {}
  categoryForm.value = { name: row ? row.name : '' }
  categoryDialogVisible.value = true
}

function saveCategory() {
  if (!categoryForm.value.name.trim()) return ElMessage.warning('请输入分类名称')
  categorySaving.value = true
  const data = { name: categoryForm.value.name.trim() }
  const req = editingCategory.value.id
    ? request.put(`/admin/categories/${editingCategory.value.id}`, data)
    : request.post('/admin/categories', data)
  req.then(() => {
    ElMessage.success(editingCategory.value.id ? '更新成功' : '创建成功')
    categoryDialogVisible.value = false
    fetchCategories()
  }).finally(() => categorySaving.value = false)
}

function handleDeleteCategory(row) {
  request.delete(`/admin/categories/${row.id}`).then(() => {
    ElMessage.success('已删除')
    fetchCategories()
  })
}

// --- 标签管理 ---
const tags = ref([])
const tagDialogVisible = ref(false)
const tagSaving = ref(false)
const editingTag = ref({})
const tagForm = ref({ name: '' })

function fetchTags() {
  request.get('/tags').then(res => { tags.value = res || [] })
}

function showTagDialog(row) {
  editingTag.value = row || {}
  tagForm.value = { name: row ? row.name : '' }
  tagDialogVisible.value = true
}

function saveTag() {
  if (!tagForm.value.name.trim()) return ElMessage.warning('请输入标签名称')
  tagSaving.value = true
  const data = { name: tagForm.value.name.trim() }
  const req = editingTag.value.id
    ? request.put(`/admin/tags/${editingTag.value.id}`, data)
    : request.post('/admin/tags', data)
  req.then(() => {
    ElMessage.success(editingTag.value.id ? '更新成功' : '创建成功')
    tagDialogVisible.value = false
    fetchTags()
  }).finally(() => tagSaving.value = false)
}

function handleDeleteTag(row) {
  request.delete(`/admin/tags/${row.id}`).then(() => {
    ElMessage.success('已删除')
    fetchTags()
  })
}

function handleDeleteUserPost(row) {
  ElMessageBox.confirm(`确定删除文章「${row.title}」？`, '确认删除', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  }).then(() => {
    request.delete(`/admin/posts/${row.id}`).then(() => {
      ElMessage.success('已删除')
      userPosts.value = userPosts.value.filter(p => p.id !== row.id)
      fetchStats()
    })
  }).catch(() => {})
}

onMounted(() => {
  fetchStats()
  fetchUsers()
  fetchPosts()
})
</script>

<style scoped>
.admin-dashboard {
  max-width: 1100px;
  margin: 0 auto;
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
  transition: transform 0.15s;
}
.stat-card:hover { transform: translateY(-2px); }
.stat-icon {
  font-size: 36px;
  width: 60px; height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-card.users .stat-icon { background: #e8f4fd; }
.stat-card.posts .stat-icon { background: #e8f8e8; }
.stat-card.comments .stat-icon { background: #fef5e7; }
.stat-value { font-size: 28px; font-weight: 700; color: #1a1a2e; }
.stat-label { font-size: 13px; color: #909399; margin-top: 2px; }

/* Tab 切换 */
.tabs {
  display: flex;
  gap: 4px;
  background: #f5f7fa;
  border-radius: 10px;
  padding: 4px;
  margin-bottom: 20px;
  width: fit-content;
}
.tab {
  padding: 10px 24px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  cursor: pointer;
  transition: all 0.15s;
}
.tab:hover { color: #303133; }
.tab.active { background: #fff; color: #409eff; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }

/* 面板 */
.panel {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.panel-header h3 { margin: 0; font-size: 17px; font-weight: 600; }
.panel-actions { display: flex; gap: 12px; }

.post-link { color: #303133; text-decoration: none; font-weight: 500; }
.post-link:hover { color: #409eff; }

.author-cell { display: flex; align-items: center; gap: 8px; }

@media (max-width: 768px) {
  .stats-row { grid-template-columns: 1fr; }
  .panel-header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .panel-actions { width: 100%; flex-direction: column; }
  .panel-actions .el-input,
  .panel-actions .el-select { width: 100% !important; }
}
</style>
