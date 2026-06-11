<template>
  <div class="home">
    <!-- 顶部Banner -->
    <div class="banner">
      <h1>欢迎来到博客世界</h1>
      <p>分享知识，记录生活</p>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 左侧文章列表 -->
      <div class="left-content">
        <!-- 搜索框 -->
        <div class="search-box">
          <input v-model="searchKeyword" type="text" placeholder="搜索文章..." @keyup.enter="searchPosts">
          <button @click="searchPosts">搜索</button>
        </div>

        <!-- 文章列表 -->
        <div class="article-list">
          <div v-if="loading" class="loading">加载中...</div>
          <div v-else-if="posts.length === 0" class="empty">暂无文章</div>
          <div v-else class="article-item" v-for="post in posts" :key="post.id" @click="goToPost(post.id)">
            <div class="article-cover" v-if="post.coverImage">
              <img :src="getImageUrl(post.coverImage)" :alt="post.title">
            </div>
            <div class="article-info">
              <h3 class="article-title">{{ post.title }}</h3>
              <div class="article-tags" v-if="post.categoryName || (post.tagNames && post.tagNames.length)">
                <el-tag v-if="post.categoryName" type="primary" size="small" effect="plain">
                  {{ post.categoryName }}
                </el-tag>
                <el-tag
                  v-for="tag in post.tagNames"
                  :key="tag"
                  size="small"
                  effect="plain"
                  style="margin-left:4px"
                >{{ tag }}</el-tag>
              </div>
              <div class="article-meta">
                <span class="article-date">{{ formatDate(post.createTime) }}</span>
              </div>
              <p class="article-summary">{{ truncateContent(post) }}</p>
              <div class="article-footer">
                <span class="article-author">作者: {{ post.authorName || '匿名' }}</span>
                <span class="article-views">阅读: {{ post.viewCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="totalPages > 1">
          <button @click="changePage(currentPage - 1)" :disabled="currentPage <= 1">上一页</button>
          <span>第 {{ currentPage }} / {{ totalPages }} 页</span>
          <button @click="changePage(currentPage + 1)" :disabled="currentPage >= totalPages">下一页</button>
        </div>
      </div>

      <!-- 右侧侧边栏 -->
      <div class="right-sidebar">
        <!-- 文章分类 -->
        <div class="sidebar-card">
          <h3>文章分类</h3>
          <div class="category-list">
            <div
              class="category-item"
              :class="{ active: !selectedCategory }"
              @click="selectCategory(null)"
            >
              <span class="category-name">全部</span>
              <span class="category-count">{{ total }}</span>
            </div>
            <div
              class="category-item"
              v-for="cat in categories"
              :key="cat.id"
              :class="{ active: selectedCategory === cat.id }"
              @click="selectCategory(cat.id)"
            >
              <span class="category-name">{{ cat.name }}</span>
            </div>
          </div>
        </div>

        <!-- 热门文章 -->
        <div class="sidebar-card">
          <h3>热门文章</h3>
          <div class="popular-list">
            <div class="popular-item" v-for="(post, index) in popularPosts" :key="post.id"
                 @click="goToPost(post.id)">
              <span class="rank">{{ index + 1 }}</span>
              <span class="title">{{ post.title }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()

const posts = ref([])
const popularPosts = ref([])
const categories = ref([])
const selectedCategory = ref(null)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const truncateContent = (post) => {
  const text = (post.summary || post.content || '').replace(/<[^>]*>/g, '')
  return text.substring(0, 100) + '...'
}

const fetchPosts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }

    const res = await request.get('/posts', { params })

    if (res && res.records) {
      posts.value = res.records
      total.value = res.total || posts.value.length
    } else {
      posts.value = []
      total.value = 0
    }
  } catch (err) {
    console.error('获取文章失败:', err)
    posts.value = []
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await request.get('/categories')
    categories.value = res || []
  } catch (err) {
    console.error('获取分类失败:', err)
  }
}

const selectCategory = (catId) => {
  selectedCategory.value = catId
  currentPage.value = 1
  fetchPosts()
}

const fetchPopularPosts = async () => {
  try {
    const res = await request.get('/posts/popular')
    popularPosts.value = res || []
  } catch (err) {
    console.error('获取热门文章失败:', err)
    popularPosts.value = []
  }
}

const searchPosts = () => {
  currentPage.value = 1
  fetchPosts()
}

const changePage = (page) => {
  currentPage.value = page
  fetchPosts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToPost = (id) => {
  router.push(`/post/${id}`)
}

const getImageUrl = (path) => {
  if (!path) return '/default-cover.jpg'
  if (path.startsWith('http')) return path
  if (path.startsWith('/uploads/')) return path
  return `/uploads/${path}`
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

onMounted(() => {
  fetchPosts()
  fetchPopularPosts()
  fetchCategories()
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: #f5f5f5;
}

.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 60px 20px;
  text-align: center;
}

.banner h1 {
  font-size: 2.5em;
  margin-bottom: 10px;
}

.banner p {
  font-size: 1.2em;
  opacity: 0.9;
}

.main-content {
  max-width: 1200px;
  margin: 30px auto;
  padding: 0 20px;
  display: flex;
  gap: 30px;
}

.left-content {
  flex: 1;
  min-width: 0;
}

.right-sidebar {
  width: 300px;
}

.search-box {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.search-box input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.search-box button {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-item {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.article-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
}

.article-cover {
  height: 200px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-info {
  padding: 20px;
}

.article-title {
  font-size: 1.3em;
  margin-bottom: 6px;
  color: #333;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}

.article-meta {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 0.9em;
  margin-bottom: 10px;
}

.article-summary {
  color: #666;
  line-height: 1.6;
  margin-bottom: 15px;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 0.9em;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 30px;
  background: white;
  padding: 15px;
  border-radius: 8px;
}

.pagination button {
  padding: 8px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.sidebar-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.sidebar-card h3 {
  font-size: 1.1em;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #667eea;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.category-item:hover {
  background: #f0f0ff;
}

.category-item.active {
  background: #667eea;
  color: white;
}

.category-item.active .category-count {
  color: rgba(255,255,255,0.8);
}

.category-name {
  font-size: 0.95em;
}

.category-count {
  font-size: 0.8em;
  color: #999;
  background: #f0f0f0;
  border-radius: 10px;
  padding: 2px 8px;
}

.popular-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.popular-item {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px;
}

.popular-item:hover .title {
  color: #667eea;
}

.popular-item .rank {
  width: 20px;
  height: 20px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8em;
  flex-shrink: 0;
}

.popular-item .title {
  flex: 1;
  font-size: 0.9em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #999;
  background: white;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }

  .right-sidebar {
    width: 100%;
  }
}
</style>
