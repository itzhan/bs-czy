<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background:#304156;">
      <div style="height:60px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:18px;font-weight:bold;">
        校园知乎 Admin
      </div>
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background:#fff;display:flex;align-items:center;justify-content:flex-end;box-shadow:0 1px 4px rgba(0,0,0,.08);">
        <span style="margin-right:16px;color:#606266;">{{ username }}</span>
        <el-button type="danger" text @click="logout">退出登录</el-button>
      </el-header>
      <el-main style="background:#f0f2f5;overflow-y:auto;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const menuItems = [
  { path: '/dashboard', title: '数据概览', icon: 'DataAnalysis' },
  { path: '/users', title: '用户管理', icon: 'User' },
  { path: '/questions', title: '问题管理', icon: 'ChatDotSquare' },
  { path: '/answers', title: '回答管理', icon: 'Comment' },
  { path: '/comments', title: '评论管理', icon: 'ChatLineSquare' },
  { path: '/tags', title: '标签管理', icon: 'Collection' },
  { path: '/reports', title: '举报管理', icon: 'Warning' },
]

const username = computed(() => localStorage.getItem('admin_username') || '管理员')

const logout = () => {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_username')
  router.push('/login')
}
</script>
