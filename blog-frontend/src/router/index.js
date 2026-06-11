import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getUserInfo } from '../utils/auth'
import { ElMessage } from 'element-plus'
import MainLayout from '../layouts/MainLayout.vue'

const routes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: { title: '登录', guest: true }
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('../views/Register.vue'),
        meta: { title: '注册', guest: true }
      },
      {
        path: 'post/:id',
        name: 'PostDetail',
        component: () => import('../views/PostDetail.vue'),
        meta: { title: '文章详情' }
      },
      {
        path: 'user/:id',
        name: 'UserProfile',
        component: () => import('../views/UserProfile.vue'),
        meta: { title: '用户主页' }
      },
      {
        path: 'editor',
        name: 'Editor',
        component: () => import('../views/Editor.vue'),
        meta: { title: '写文章', requiresAuth: true }
      },
      {
        path: 'editor/:id',
        name: 'EditPost',
        component: () => import('../views/Editor.vue'),
        meta: { title: '编辑文章', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('../views/Admin.vue'),
        meta: { title: '管理后台', requiresAuth: true, admin: true }
      },
      // Redirect legacy routes
      {
        path: 'create',
        redirect: '/editor'
      },
      {
        path: 'edit/:id',
        redirect: to => ({ path: `/editor/${to.params.id}` })
      },
      {
        path: 'category/:id',
        redirect: '/'
      },
      {
        path: 'my-posts',
        redirect: '/profile'
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 更新页面标题
  document.title = to.meta.title ? `${to.meta.title} - 轻博客` : '轻博客'

  const token = getToken()
  const userInfo = getUserInfo()

  // 需要登录的页面
  if (to.meta.requiresAuth && !token) {
    ElMessage.warning('请先登录')
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 需要管理员权限的页面
  if (to.meta.admin && (!userInfo || userInfo.role !== 0)) {
    ElMessage.warning('无权访问管理后台')
    next({ name: 'Home' })
    return
  }

  // 已登录用户访问游客页面（如登录页）
  if (to.meta.guest && token) {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router
