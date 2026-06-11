<template>
  <div class="user-profile">
    <div v-if="loading" class="loading">
      <span>加载中...</span>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
    </div>

    <div v-else-if="user" class="profile-content">
      <!-- 用户信息卡片 -->
      <div class="user-card">
        <img :src="user.avatar || '/default-avatar.svg'" alt="avatar" class="avatar" />
        <div class="user-info">
          <h1>{{ user.username }}</h1>
          <p class="bio">{{ user.bio || '这个人很懒，什么都没写' }}</p>

          <div class="stats">
            <div class="stat-item">
              <span class="number">{{ user.postCount || 0 }}</span>
              <span class="label">文章</span>
            </div>
            <div class="stat-item">
              <span class="number">{{ user.followerCount || 0 }}</span>
              <span class="label">粉丝</span>
            </div>
            <div class="stat-item">
              <span class="number">{{ user.followingCount || 0 }}</span>
              <span class="label">关注</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 文章列表 -->
      <div class="posts-section">
        <h2>📝 他的文章</h2>

        <div v-if="postsLoading" class="loading">加载文章中...</div>

        <div v-else-if="posts.length === 0" class="empty">
          暂无文章
        </div>

        <div v-else class="post-list">
          <div
            v-for="post in posts"
            :key="post.id"
            class="post-item"
            @click="goToPost(post.id)"
          >
            <div class="post-header">
              <span class="date">{{ formatDate(post.createTime) }}</span>
            </div>
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-summary">{{ truncateContent(post) }}</p>
            <div class="post-stats">
              <span>👁️ {{ post.viewCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <button class="back-btn" @click="$router.back()">← 返回</button>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()

const user = ref(null)
const posts = ref([])
const loading = ref(true)
const postsLoading = ref(false)
const error = ref(null)

const userId = computed(() => route.params.id)

const truncateContent = (post) => {
  const text = (post.summary || post.content || '').replace(/<[^>]*>/g, '')
  if (text.length <= 100) return text
  return text.substring(0, 100) + '...'
}

const fetchUserProfile = async () => {
  loading.value = true
  error.value = null

  try {
    const res = await request.get('/posts', {
      params: { page: 1, size: 100 }
    })

    const allPosts = res?.records || []
    const userPosts = allPosts.filter(p => p.authorId === parseInt(userId.value))

    if (userPosts.length > 0) {
      const firstPost = userPosts[0]
      user.value = {
        id: firstPost.authorId,
        username: firstPost.authorName || '未知用户',
        avatar: firstPost.authorAvatar,
        postCount: userPosts.length
      }
      posts.value = userPosts
    } else {
      error.value = '用户不存在或暂无文章'
    }
  } catch (err) {
    console.error('获取用户信息失败:', err)
    error.value = '加载用户信息失败'
  } finally {
    loading.value = false
  }
}

const goToPost = (id) => {
  router.push(`/post/${id}`)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  fetchUserProfile()
})
</script>

<style scoped>
.user-profile {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 20px;
}

.loading, .error {
  text-align: center;
  padding: 100px;
  color: #999;
}

.user-card {
  display: flex;
  gap: 30px;
  background: white;
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin-bottom: 30px;
}

.avatar {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #f0f0f0;
}

.user-info {
  flex: 1;
}

.user-info h1 {
  font-size: 2em;
  margin-bottom: 10px;
  color: #333;
}

.bio {
  color: #666;
  margin-bottom: 20px;
  line-height: 1.6;
}

.stats {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-item .number {
  display: block;
  font-size: 1.8em;
  font-weight: bold;
  color: #667eea;
}

.stat-item .label {
  color: #999;
  font-size: 14px;
}

.posts-section {
  background: white;
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.posts-section h2 {
  margin-bottom: 20px;
  color: #333;
}

.empty {
  text-align: center;
  padding: 60px;
  color: #999;
}

.post-list {
  display: grid;
  gap: 20px;
}

.post-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.post-item:hover {
  transform: translateX(10px);
  background: #f9f9f9;
}

.post-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 13px;
}

.date {
  color: #999;
}

.post-title {
  font-size: 1.2em;
  margin: 10px 0;
  color: #333;
}

.post-summary {
  color: #666;
  line-height: 1.6;
  margin-bottom: 15px;
}

.post-stats {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 13px;
}

.back-btn {
  margin-top: 30px;
  padding: 10px 20px;
  background: #f0f0f0;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.back-btn:hover {
  background: #e0e0e0;
}

@media (max-width: 768px) {
  .user-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .stats {
    justify-content: center;
  }
}
</style>
