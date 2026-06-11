<template>
  <div class="comment-item">
    <img :src="comment.avatar || '/default-avatar.svg'" alt="avatar" />
    <div class="comment-body">
      <div class="comment-header">
        <span class="username" @click="goToUser(comment.userId)">
          {{ comment.guestName || comment.username || '游客' }}
          <span v-if="comment.userId === postAuthorId" class="author-badge">作者</span>
        </span>
        <span class="date">{{ formatDate(comment.createTime) }}</span>
      </div>
      <p class="comment-content">{{ comment.content }}</p>
    </div>
  </div>
  <!-- 嵌套回复 -->
  <div v-if="comment.replies && comment.replies.length > 0" class="nested-replies">
    <CommentItem
      v-for="reply in comment.replies"
      :key="reply.id"
      :comment="reply"
      :post-author-id="postAuthorId"
    />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  comment: { type: Object, required: true },
  postAuthorId: { type: Number, default: null }
})

const router = useRouter()

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const goToUser = (userId) => {
  if (userId) {
    router.push(`/user/${userId}`)
  }
}
</script>

<style scoped>
.comment-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
}

.comment-item img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-header .username {
  font-weight: bold;
  cursor: pointer;
  color: #333;
}

.comment-header .username:hover {
  color: #667eea;
}

.author-badge {
  background: #667eea;
  color: white;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 3px;
  margin-left: 5px;
}

.comment-header .date {
  color: #999;
  font-size: 13px;
}

.comment-content {
  margin: 10px 0;
  line-height: 1.6;
  color: #333;
}

.nested-replies {
  margin-left: 50px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 5px;
}
</style>
