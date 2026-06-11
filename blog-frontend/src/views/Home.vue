<template>
  <div class="home">
    <div class="post-list">
      <el-card v-for="post in posts" :key="post.id" class="post-card" shadow="hover" @click="$router.push(`/post/${post.id}`)">
        <el-image v-if="post.coverImage" :src="post.coverImage" fit="cover" class="cover" />
        <h3>{{ post.title }}</h3>
        <p class="excerpt">{{ stripHtml(post.content).slice(0, 150) }}...</p>
        <div class="meta">
          <span><el-icon><User /></el-icon> {{ post.authorName }}</span>
          <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
          <span><el-icon><Clock /></el-icon> {{ formatDate(post.createTime) }}</span>
        </div>
      </el-card>
    </div>
    <el-empty v-if="!loading && posts.length === 0" description="暂无文章" />
    <el-pagination
      v-if="total > size"
      v-model:current-page="page" :total="total" :page-size="size"
      layout="prev, pager, next" @current-change="fetchPosts" style="justify-content:center;margin-top:20px"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const posts = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

function stripHtml(html) {
  return (html || '').replace(/<[^>]+>/g, '').replace(/[#*`>\[\]!\-]/g, '')
}

function formatDate(d) {
  return d ? d.replace('T', ' ').substring(0, 19) : ''
}

function fetchPosts() {
  loading.value = true
  request.get('/posts', { params: { page: page.value, size: size.value, keyword: route.query.keyword || '' } }).then(res => {
    posts.value = res.records
    total.value = res.total
  }).finally(() => loading.value = false)
}

onMounted(fetchPosts)
watch(() => route.query.keyword, () => { page.value = 1; fetchPosts() })
</script>

<style scoped>
.home { max-width: 900px; margin: 0 auto; }
.post-card { margin-bottom: 16px; cursor: pointer; }
.post-card .cover { width: 100%; height: 200px; border-radius: 4px; margin-bottom: 12px; object-fit: cover; }
.post-card h3 { margin: 0 0 8px; }
.excerpt { color: #666; font-size: 14px; margin-bottom: 12px; line-height: 1.6; }
.meta { display: flex; gap: 20px; color: #999; font-size: 13px; }
.meta span { display: flex; align-items: center; gap: 4px; }
</style>
