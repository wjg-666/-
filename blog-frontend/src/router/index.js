import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'post/:id', name: 'PostDetail', component: () => import('@/views/PostDetail.vue') },
      { path: 'login', name: 'Login', component: () => import('@/views/Login.vue') },
      { path: 'register', name: 'Register', component: () => import('@/views/Register.vue') },
      { path: 'editor', name: 'Editor', component: () => import('@/views/Editor.vue') },
      { path: 'editor/:id', name: 'EditPost', component: () => import('@/views/Editor.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue') },
      { path: 'admin', name: 'Admin', component: () => import('@/views/Admin.vue') },
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })
export default router
