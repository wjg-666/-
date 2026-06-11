<template>
  <div class="post-page">
    <!-- 阅读进度条 -->
    <div class="progress-bar">
      <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
    </div>

    <!-- 顶部导航栏 -->
    <div class="top-bar">
      <button class="back-btn" @click="$router.push('/')">
        <span class="back-arrow">←</span>
        <span>返回首页</span>
      </button>
      <div class="breadcrumb">
        <router-link to="/">首页</router-link>
        <span class="sep">/</span>
        <span>文章详情</span>
      </div>
    </div>

    <div class="layout" v-if="post">
      <!-- 左侧：正文区 -->
      <article class="main-content">
        <!-- 封面图 -->
        <div v-if="post.coverImage" class="cover-wrap">
          <img :src="post.coverImage" class="cover-img" alt="封面图" />
        </div>

        <!-- 标题 -->
        <h1 class="post-title">{{ post.title }}</h1>

        <!-- 元信息栏 -->
        <div class="meta-bar">
          <div class="meta-left">
            <router-link :to="`/profile`" class="author-chip">
              <el-avatar :size="32" :src="post.authorAvatar" class="chip-avatar">
                {{ (post.authorName || '?')[0] }}
              </el-avatar>
              <span class="chip-name">{{ post.authorName }}</span>
            </router-link>
            <span class="meta-dot">·</span>
            <span class="meta-item">
              <span class="meta-icon">📅</span>
              {{ formatDate(post.createTime) }}
            </span>
            <span class="meta-dot">·</span>
            <span class="meta-item">
              <span class="meta-icon">⏱️</span>
              {{ readingTime }}
            </span>
            <span class="meta-dot">·</span>
            <span class="meta-item">
              <span class="meta-icon">👁️</span>
              {{ post.viewCount }} 阅读
            </span>
          </div>
          <div class="meta-right">
            <el-tag v-if="post.status === 0" type="info" size="small">草稿</el-tag>
          </div>
        </div>

        <el-divider />

        <!-- Markdown 正文 -->
        <div class="content" ref="contentRef" v-html="renderMarkdown(post.content)"></div>

        <!-- 底部分隔 -->
        <el-divider class="end-divider">
          <span class="end-text">— 全文完 —</span>
        </el-divider>

        <!-- 文章操作 -->
        <div class="article-actions">
          <button class="action-btn" @click="copyLink">
            <span>🔗</span> 复制链接
          </button>
          <button class="action-btn" @click="scrollToComments">
            <span>💬</span> 评论 ({{ commentCount }})
          </button>
          <button class="action-btn" @click="scrollToTop">
            <span>⬆</span> 回到顶部
          </button>
        </div>

        <!-- 作者卡片 -->
        <div class="author-card">
          <div class="ac-bg"></div>
          <div class="ac-body">
            <el-avatar :size="56" :src="post.authorAvatar" class="ac-avatar">
              {{ (post.authorName || '?')[0] }}
            </el-avatar>
            <div class="ac-info">
              <strong class="ac-name">{{ post.authorName }}</strong>
              <p class="ac-bio">博主 · 热爱技术与写作</p>
              <p class="ac-date">{{ formatDate(post.createTime) }} 加入</p>
            </div>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comment-section" ref="commentSectionRef">
          <h3 class="section-title">
            <span>评论</span>
            <span class="comment-badge">{{ commentCount }}</span>
          </h3>

          <!-- 未登录提示 -->
          <div v-if="!userStore.user" class="login-banner">
            <div class="banner-content">
              <span class="banner-icon">🔐</span>
              <div class="banner-text">
                <strong>登录后参与讨论</strong>
                <p>登录你的账户，与作者和其他读者交流你的想法</p>
              </div>
            </div>
            <el-button type="primary" round size="default" @click="$router.push('/login')">立即登录</el-button>
          </div>

          <div class="comment-form">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="userStore.user ? 3 : 2"
              :placeholder="userStore.user ? '写下你的评论...' : '或留下昵称，以游客身份评论...'"
            />
            <div class="form-actions">
              <el-input
                v-if="!userStore.user"
                v-model="guestName"
                placeholder="你的昵称"
                class="guest-input"
              />
              <el-button type="primary" @click="submitComment" :disabled="!commentContent.trim()">
                发表评论
              </el-button>
            </div>
          </div>

          <div class="comments-list">
            <div v-for="c in comments" :key="c.id" class="comment-item">
              <div class="comment-header">
                <el-avatar :size="36" :src="c.avatar" class="comment-avatar">
                  {{ (c.username || c.guestName || '匿')[0] }}
                </el-avatar>
                <div class="comment-meta">
                  <strong>{{ c.username || c.guestName || '匿名用户' }}</strong>
                  <span class="time">{{ formatDate(c.createTime) }}</span>
                </div>
                <el-button text size="small" class="reply-btn" @click="reply(c)">回复</el-button>
              </div>
              <p class="comment-content">{{ c.content }}</p>
              <div v-for="r in c.replies" :key="r.id" class="reply-item">
                <div class="reply-header">
                  <el-avatar :size="24" :src="r.avatar" class="reply-avatar">
                    {{ (r.username || r.guestName || '匿')[0] }}
                  </el-avatar>
                  <strong>{{ r.username || r.guestName || '匿名用户' }}</strong>
                  <span class="time">{{ formatDate(r.createTime) }}</span>
                </div>
                <p class="reply-content">{{ r.content }}</p>
              </div>
            </div>
            <el-empty v-if="comments.length === 0" description="暂无评论，来发表第一条吧" :image-size="80" />
          </div>
        </div>
      </article>

      <!-- 右侧：目录侧栏 -->
      <aside class="toc-sidebar" v-if="tocItems.length > 0">
        <div class="toc-card">
          <h4 class="toc-title">📑 目录</h4>
          <nav class="toc-nav">
            <a
              v-for="(item, idx) in tocItems"
              :key="idx"
              :class="['toc-link', 'toc-' + item.level, { active: activeTocIdx === idx }]"
              :style="{ paddingLeft: (item.level === 'h1' ? 0 : item.level === 'h2' ? 12 : 24) + 'px' }"
              @click.prevent="scrollToHeading(item.id)"
            >{{ item.text }}</a>
          </nav>
        </div>
      </aside>
    </div>

    <!-- 加载态 -->
    <div v-else class="loading-state">
      <el-skeleton :rows="3" animated />
      <el-divider />
      <el-skeleton :rows="8" animated />
    </div>

    <!-- 返回顶部浮动按钮 -->
    <Transition name="fade">
      <button v-if="showBackTop" class="fab" @click="scrollToTop" title="回到顶部">⬆</button>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref(null)
const comments = ref([])
const commentContent = ref('')
const guestName = ref('')
const replyTo = ref(null)
const showBackTop = ref(false)
const progressPercent = ref(0)
const tocItems = ref([])
const activeTocIdx = ref(0)
const contentRef = ref(null)
const commentSectionRef = ref(null)
let scrollHandler = null

// --- 计算属性 ---
const commentCount = computed(() => {
  let count = comments.value.length
  comments.value.forEach(c => { count += (c.replies || []).length })
  return count
})

const readingTime = computed(() => {
  if (!post.value?.content) return '少于1分钟'
  const text = post.value.content.replace(/[#*`>\[\]!\-\(\)|\\~`]/g, '').replace(/\s+/g, '')
  const minutes = Math.ceil(text.length / 400)
  return minutes <= 1 ? '约1分钟' : `约${minutes}分钟`
})

// --- 工具函数 ---
function renderMarkdown(text) {
  if (!text) return ''
  return marked(text)
}

function formatDate(d) {
  return d ? d.replace('T', ' ').substring(0, 16) : ''
}

// --- 目录提取 ---
function extractTOC() {
  if (!post.value?.content) return
  const headings = post.value.content.match(/^#{1,3}\s+.+$/gm)
  if (!headings) { tocItems.value = []; return }
  tocItems.value = headings.map(h => {
    const match = h.match(/^(#{1,3})\s+(.+)$/)
    const level = match[1].length === 1 ? 'h1' : match[1].length === 2 ? 'h2' : 'h3'
    const text = match[2]
    const id = 'heading-' + text.replace(/[^\w一-鿿]/g, '-').replace(/-+/g, '-').toLowerCase()
    return { level, text, id }
  })
}

// 给正文中每个标题注入 id，方便目录跳转
function injectHeadingIds() {
  nextTick(() => {
    if (!contentRef.value) return
    const headings = contentRef.value.querySelectorAll('h1, h2, h3')
    tocItems.value.forEach((item, idx) => {
      if (headings[idx]) {
        headings[idx].id = item.id
        headings[idx].classList.add('scroll-heading')
      }
    })
  })
}

function scrollToHeading(id) {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// --- 滚动监听 ---
function onScroll() {
  const scrollTop = window.scrollY || document.documentElement.scrollTop
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  progressPercent.value = docHeight > 0 ? Math.min((scrollTop / docHeight) * 100, 100) : 0
  showBackTop.value = scrollTop > 400

  // 高亮当前目录项
  if (tocItems.value.length > 0) {
    let current = 0
    tocItems.value.forEach((item, idx) => {
      const el = document.getElementById(item.id)
      if (el && el.getBoundingClientRect().top < 120) {
        current = idx
      }
    })
    activeTocIdx.value = current
  }
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function scrollToComments() {
  if (commentSectionRef.value) {
    commentSectionRef.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function copyLink() {
  navigator.clipboard.writeText(window.location.href).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.info('复制失败，请手动复制地址栏链接')
  })
}

// --- 评论 ---
function fetchData() {
  request.get(`/posts/${route.params.id}`).then(res => {
    post.value = res
    nextTick(() => {
      extractTOC()
      injectHeadingIds()
    })
  })
  request.get(`/posts/${route.params.id}/comments`).then(res => comments.value = res)
}

function reply(c) {
  replyTo.value = c.id
  commentContent.value = `@${c.username || c.guestName || '匿名'} `
}

function submitComment() {
  if (!commentContent.value.trim()) return
  request.post(`/posts/${route.params.id}/comments`, {
    content: commentContent.value,
    guestName: guestName.value || undefined,
    parentId: replyTo.value || undefined
  }).then(() => {
    commentContent.value = ''
    replyTo.value = null
    guestName.value = ''
    fetchData()
  })
}

onMounted(() => {
  fetchData()
  scrollHandler = onScroll
  window.addEventListener('scroll', scrollHandler, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', scrollHandler)
})

watch(() => route.params.id, () => {
  fetchData()
  scrollToTop()
  progressPercent.value = 0
})
</script>

<style scoped>
/* ====== 全局布局 ====== */
.post-page {
  min-height: 100vh;
  background: #f8f9fb;
  padding-bottom: 60px;
}

/* ====== 阅读进度条 ====== */
.progress-bar {
  position: fixed;
  top: 0; left: 0;
  width: 100%;
  height: 3px;
  background: #e8e8e8;
  z-index: 1001;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #67c23a);
  transition: width 0.1s linear;
}

/* ====== 顶部导航 ====== */
.top-bar {
  max-width: 1060px;
  margin: 0 auto;
  padding: 16px 20px 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.15s;
}
.back-btn:hover {
  background: #e8e8ec;
  color: #409eff;
}
.back-arrow {
  font-size: 18px;
  line-height: 1;
}
.breadcrumb {
  font-size: 13px;
  color: #909399;
}
.breadcrumb a {
  color: #909399;
  text-decoration: none;
}
.breadcrumb a:hover { color: #409eff; }
.breadcrumb .sep { margin: 0 6px; }

/* ====== 双栏布局 ====== */
.layout {
  max-width: 1060px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  gap: 28px;
  align-items: flex-start;
}
.main-content {
  flex: 1;
  min-width: 0;
}

/* ====== 封面图 ====== */
.cover-wrap {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
}
.cover-img {
  width: 100%;
  max-height: 420px;
  object-fit: cover;
  display: block;
}

/* ====== 标题 ====== */
.post-title {
  font-size: 30px;
  font-weight: 800;
  color: #1a1a2e;
  line-height: 1.35;
  margin: 0 0 18px;
  letter-spacing: -0.3px;
}

/* ====== 元信息 ====== */
.meta-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}
.meta-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.author-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: inherit;
  padding: 3px 10px 3px 3px;
  border-radius: 20px;
  background: #f0f2f5;
  transition: background 0.15s;
}
.author-chip:hover {
  background: #e4e7ed;
}
.chip-name {
  font-size: 14px;
  font-weight: 500;
}
.meta-dot {
  color: #c0c4cc;
  font-weight: 700;
}
.meta-item {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 3px;
}
.meta-icon {
  font-size: 13px;
}

/* ====== 正文 ====== */
.content {
  line-height: 1.95;
  font-size: 16px;
  color: #303133;
}
.content :deep(h1) { font-size: 24px; margin: 32px 0 14px; font-weight: 700; color: #1a1a2e; }
.content :deep(h2) { font-size: 20px; margin: 28px 0 12px; font-weight: 700; color: #1a1a2e; padding-bottom: 6px; border-bottom: 1px solid #eee; }
.content :deep(h3) { font-size: 17px; margin: 22px 0 10px; font-weight: 600; color: #303133; }
.content :deep(p) { margin: 14px 0; }
.content :deep(ul), .content :deep(ol) { padding-left: 24px; margin: 12px 0; }
.content :deep(li) { margin: 4px 0; }
.content :deep(a) { color: #409eff; text-decoration: none; }
.content :deep(a:hover) { text-decoration: underline; }
.content :deep(code) {
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 14px;
  color: #e65c00;
}
.content :deep(pre) {
  background: #1e1e2e;
  color: #cdd6f4;
  padding: 18px 22px;
  border-radius: 10px;
  overflow-x: auto;
  margin: 18px 0;
  line-height: 1.6;
}
.content :deep(pre code) {
  background: none;
  padding: 0;
  color: inherit;
  font-size: 14px;
}
.content :deep(blockquote) {
  border-left: 3px solid #409eff;
  padding: 8px 18px;
  margin: 16px 0;
  color: #606266;
  background: #f5f7fa;
  border-radius: 0 8px 8px 0;
}
.content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}
.content :deep(th), .content :deep(td) {
  border: 1px solid #e8e8e8;
  padding: 10px 14px;
  text-align: left;
}
.content :deep(th) {
  background: #f5f7fa;
  font-weight: 600;
}
.content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 12px 0;
}
.content :deep(hr) {
  border: none;
  border-top: 1px solid #eee;
  margin: 24px 0;
}
.scroll-heading {
  scroll-margin-top: 70px;
}

/* 正文结束分隔线 */
.end-divider {
  margin: 36px 0 12px;
}
.end-text {
  font-size: 13px;
  color: #c0c4cc;
  letter-spacing: 2px;
}

/* ====== 文章操作按钮 ====== */
.article-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 28px;
  justify-content: center;
}
.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 20px;
  padding: 8px 18px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.15s;
}
.action-btn:hover {
  border-color: #409eff;
  color: #409eff;
  background: #f0f7ff;
}

/* ====== 作者卡片 ====== */
.author-card {
  margin-bottom: 28px;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
}
.ac-bg {
  height: 60px;
  background: linear-gradient(135deg, #409eff, #67c23a);
  opacity: 0.15;
}
.ac-body {
  padding: 0 24px 24px;
  display: flex;
  gap: 16px;
  align-items: flex-start;
  margin-top: -28px;
}
.ac-avatar {
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.ac-name {
  font-size: 16px;
  color: #1a1a2e;
}
.ac-bio {
  margin: 4px 0;
  font-size: 13px;
  color: #909399;
}
.ac-date {
  margin: 0;
  font-size: 12px;
  color: #c0c4cc;
}

/* ====== 评论区 ====== */
.comment-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
}
.section-title {
  margin: 0 0 20px;
  font-size: 17px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}
.comment-badge {
  background: #409eff;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 400;
}
.login-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #f0f7ff 0%, #e8f4fd 100%);
  border: 1px solid #d0e8fa;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
}
.banner-content { display: flex; align-items: center; gap: 12px; }
.banner-icon { font-size: 26px; }
.banner-text strong { font-size: 14px; color: #1a1a2e; }
.banner-text p { margin: 2px 0 0; font-size: 12px; color: #909399; }
.comment-form { margin-bottom: 20px; }
.form-actions { display: flex; align-items: center; gap: 10px; margin-top: 8px; }
.guest-input { width: 180px; }
.comments-list { border-top: 1px solid #f0f0f0; padding-top: 8px; }
.comment-item { padding: 16px 0; border-bottom: 1px solid #f5f5f5; }
.comment-header { display: flex; align-items: center; gap: 10px; }
.comment-avatar { flex-shrink: 0; }
.comment-meta { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.comment-meta strong { font-size: 14px; color: #303133; }
.comment-content { margin: 8px 0 0 46px; font-size: 15px; color: #333; line-height: 1.7; }
.reply-btn { flex-shrink: 0; }
.reply-item { margin: 10px 0 4px 46px; padding: 10px 14px; background: #f9fafb; border-radius: 8px; }
.reply-header { display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.reply-avatar { flex-shrink: 0; }
.reply-header strong { font-size: 13px; }
.reply-content { margin: 2px 0 0 30px; font-size: 14px; color: #555; line-height: 1.6; }
.time { font-size: 12px; color: #c0c4cc; margin-left: auto; }

/* ====== 右侧目录 ====== */
.toc-sidebar {
  width: 220px;
  flex-shrink: 0;
  position: sticky;
  top: 20px;
  max-height: calc(100vh - 40px);
  overflow-y: auto;
}
.toc-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
}
.toc-title {
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}
.toc-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.toc-link {
  display: block;
  padding: 5px 8px;
  font-size: 13px;
  color: #606266;
  text-decoration: none;
  border-radius: 6px;
  line-height: 1.5;
  border-left: 2px solid transparent;
  transition: all 0.15s;
  cursor: pointer;
}
.toc-link:hover {
  color: #409eff;
  background: #f0f7ff;
}
.toc-link.active {
  color: #409eff;
  border-left-color: #409eff;
  background: #f0f7ff;
  font-weight: 500;
}
.toc-h1 { font-weight: 600; font-size: 14px; }
.toc-h2 { font-size: 13px; }
.toc-h3 { font-size: 12px; color: #909399; }

/* ====== 浮动返回按钮 ====== */
.fab {
  position: fixed;
  bottom: 40px;
  right: 30px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  font-size: 18px;
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(64,158,255,0.35);
  transition: all 0.2s;
  z-index: 999;
}
.fab:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64,158,255,0.45);
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* ====== 加载态 ====== */
.loading-state {
  max-width: 800px;
  margin: 60px auto;
  padding: 32px;
  background: #fff;
  border-radius: 12px;
}

/* ====== 响应式 ====== */
@media (max-width: 900px) {
  .toc-sidebar { display: none; }
  .layout { padding: 12px; }
  .post-title { font-size: 24px; }
}
@media (max-width: 640px) {
  .post-title { font-size: 20px; }
  .cover-img { max-height: 240px; }
  .top-bar { padding: 10px 12px 0; }
  .login-banner { flex-direction: column; gap: 10px; text-align: center; }
  .banner-content { flex-direction: column; }
  .reply-item { margin-left: 16px; }
  .comment-content { margin-left: 0; }
  .form-actions { flex-direction: column; align-items: stretch; }
  .guest-input { width: 100%; }
  .article-actions { flex-wrap: wrap; }
  .fab { bottom: 20px; right: 16px; width: 40px; height: 40px; }
}
</style>
