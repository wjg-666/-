<template>
  <div class="post-detail">
    <div v-if="loading" class="loading">
      <span>加载中...</span>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="fetchPost">重试</button>
    </div>

    <div v-else-if="post" class="content">
      <!-- 文章头部 -->
      <div class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>

        <div class="post-meta">
          <div class="author-info" @click="goToUser(post.authorId)">
            <img :src="post.authorAvatar || '/default-avatar.svg'" alt="avatar" />
            <span>{{ post.authorName || '匿名' }}</span>
          </div>
          <span class="date">{{ formatDate(post.createTime) }}</span>
          <span class="views">👁️ {{ post.viewCount || 0 }}</span>
          <span class="likes" @click="toggleLike" :class="{ liked: post.isLiked, 'clickable': isLoggedIn }" style="cursor:default">
            {{ post.isLiked ? '❤️' : '🤍' }} {{ post.likeCount || 0 }}
          </span>
        </div>

        <!-- 分类 + 标签 -->
        <div class="post-taxonomy" v-if="post.categoryName || (post.tagNames && post.tagNames.length)">
          <el-tag v-if="post.categoryName" type="primary" size="default">
            📂 {{ post.categoryName }}
          </el-tag>
          <el-tag
            v-for="tag in post.tagNames"
            :key="tag"
            size="default"
            style="margin-left:6px"
          >🏷️ {{ tag }}</el-tag>
        </div>
      </div>

      <!-- 文章内容 -->
      <div class="post-content" v-html="post.content"></div>

      <!-- 作者信息卡 -->
      <div class="author-card" @click="goToUser(post.authorId)">
        <img :src="post.authorAvatar || '/default-avatar.svg'" alt="avatar" />
        <div class="info">
          <h4>{{ post.authorName || '匿名' }}</h4>
          <p>欢迎阅读我的文章</p>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comments-section">
        <h3>💬 评论 ({{ comments.length }})</h3>

        <!-- 评论输入框 -->
        <div class="comment-input">
          <img :src="userAvatar || '/default-avatar.svg'" alt="avatar" />
          <div class="comment-form-body">
            <textarea
              v-model="commentContent"
              placeholder="写下你的评论... (游客也可以评论哦)"
              rows="3"
            ></textarea>
            <div class="comment-form-footer">
              <span v-if="!isLoggedIn" class="guest-name-wrap">
                昵称：
                <input v-model="guestName" placeholder="游客昵称" class="guest-name-input" />
              </span>
              <button @click="submitComment" :disabled="submitting">
                {{ submitting ? '发送中...' : '发表评论' }}
              </button>
            </div>
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-if="commentsLoading" class="loading">加载评论中...</div>
          <div v-else-if="comments.length === 0" class="empty">
            还没有评论，快来抢沙发！
          </div>
          <div v-else>
            <CommentItem
              v-for="comment in comments"
              :key="comment.id"
              :comment="comment"
              :post-author-id="post.authorId"
            />
          </div>
        </div>
      </div>

      <!-- 返回按钮 -->
      <button class="back-btn" @click="$router.back()">← 返回</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getToken, getUserInfo } from '@/utils/auth'
import CommentItem from '@/components/CommentItem.vue'

const router = useRouter()
const route = useRoute()

const post = ref(null)
const comments = ref([])
const loading = ref(true)
const commentsLoading = ref(false)
const error = ref(null)
const commentContent = ref('')
const submitting = ref(false)

// 获取用户头像和登录状态
const userAvatar = ref('')
const isLoggedIn = ref(false)
const guestName = ref('')
const userInfo = getUserInfo()
if (userInfo && userInfo.avatar) {
  userAvatar.value = userInfo.avatar
}
isLoggedIn.value = !!getToken()

// 获取文章详情
const fetchPost = async () => {
  loading.value = true
  error.value = null

  try {
    const postId = route.params.id
    const data = await request.get(`/posts/${postId}`)

    if (data && typeof data === 'object') {
      post.value = data
    } else {
      error.value = '文章不存在'
    }
  } catch (err) {
    console.error('获取文章失败:', err)
    error.value = '加载文章失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 获取评论列表
const fetchComments = async () => {
  commentsLoading.value = true

  try {
    const postId = route.params.id
    const data = await request.get(`/posts/${postId}/comments`)
    // request 拦截器可能把数据放到 data 里
    if (Array.isArray(data)) {
      comments.value = data
    } else {
      comments.value = []
    }
  } catch (err) {
    console.error('获取评论失败:', err)
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

// 提交评论
const submitComment = async () => {
  const content = commentContent.value.trim()

  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }

  if (content.length > 500) {
    ElMessage.warning('评论内容不能超过500字')
    return
  }

  // 游客必须有昵称
  if (!isLoggedIn.value && !guestName.value.trim()) {
    ElMessage.warning('请输入你的昵称')
    return
  }

  submitting.value = true
  try {
    const postId = post.value.id
    const body = { content }

    // 未登录用户需要提供游客昵称
    if (!isLoggedIn.value) {
      body.guestName = guestName.value.trim()
    }

    await request.post(`/posts/${postId}/comments`, body)

    ElMessage.success('评论发表成功！')
    commentContent.value = ''
    if (!isLoggedIn.value) guestName.value = ''
    fetchComments()
  } catch (err) {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 点赞/取消点赞
const toggleLike = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  try {
    const res = await request.post(`/posts/${post.value.id}/like`)
    if (res) {
      post.value.isLiked = res.liked
      post.value.likeCount = res.liked
        ? (post.value.likeCount || 0) + 1
        : Math.max(0, (post.value.likeCount || 0) - 1)
    }
  } catch (err) {
    // handled by interceptor
  }
}

// 跳转用户主页
const goToUser = (userId) => {
  router.push(`/user/${userId}`)
}

onMounted(() => {
  fetchPost()
  fetchComments()
})
</script>

<style scoped>
.post-detail {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 20px;
}

.loading, .error {
  text-align: center;
  padding: 100px;
  color: #999;
}

.error button {
  background: #667eea;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  margin-top: 15px;
}

.post-header {
  margin-bottom: 30px;
}

.post-title {
  font-size: 2.2em;
  margin: 15px 0;
  color: #333;
  line-height: 1.4;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #666;
  margin: 15px 0;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.author-info img {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  object-fit: cover;
}

.author-info:hover {
  color: #667eea;
}

.likes {
  cursor: default;
  user-select: none;
}

.likes.clickable {
  cursor: pointer;
}

.likes.clickable:hover {
  opacity: 0.7;
}

.likes.liked {
  animation: heartBeat 0.3s ease;
}

@keyframes heartBeat {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

.post-taxonomy {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 15px;
}

.post-content {
  line-height: 1.8;
  font-size: 16px;
  color: #333;
  margin: 30px 0;
}

.post-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 20px 0;
}

.post-content :deep(pre) {
  background: #f6f8fa;
  padding: 15px;
  border-radius: 5px;
  overflow-x: auto;
}

.post-content :deep(code) {
  background: #f0f0f0;
  padding: 2px 5px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

.author-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 10px;
  cursor: pointer;
  margin: 30px 0;
}

.author-card img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

.author-card .info h4 {
  margin: 0 0 10px;
  color: #333;
}

.author-card .info p {
  color: #666;
  font-size: 14px;
  margin: 0 0 10px;
}

.comments-section {
  margin-top: 40px;
}

.comments-section h3 {
  margin-bottom: 20px;
}

.comment-input {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.comment-input img {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-form-body {
  flex: 1;
}

.comment-form-body textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: vertical;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}

.comment-form-body textarea:focus {
  border-color: #667eea;
}

.comment-form-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.guest-name-wrap {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
}

.guest-name-input {
  width: 130px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 13px;
  outline: none;
}

.guest-name-input:focus {
  border-color: #667eea;
}

.comment-form-footer button {
  padding: 8px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  margin-left: auto;
}

.comment-form-footer button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

.back-btn {
  margin-top: 30px;
  padding: 10px 20px;
  background: #f0f0f0;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background: #e0e0e0;
}
</style>
