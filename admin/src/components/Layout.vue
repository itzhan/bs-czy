<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible theme="dark" width="240">
      <div class="logo">
        <span v-if="!collapsed">🎓 校园知乎 Admin</span>
        <span v-else>🎓</span>
      </div>
      <a-menu
        :selectedKeys="[$route.path]"
        mode="inline"
        theme="dark"
        @click="({ key }) => $router.push(key)"
      >
        <a-menu-item key="/dashboard">
          <template #icon><DashboardOutlined /></template>
          数据概览
        </a-menu-item>
        <a-menu-item key="/users">
          <template #icon><UserOutlined /></template>
          用户管理
        </a-menu-item>
        <a-menu-item key="/questions">
          <template #icon><QuestionCircleOutlined /></template>
          问题管理
        </a-menu-item>
        <a-menu-item key="/answers">
          <template #icon><MessageOutlined /></template>
          回答管理
        </a-menu-item>
        <a-menu-item key="/comments">
          <template #icon><CommentOutlined /></template>
          评论管理
        </a-menu-item>
        <a-menu-item key="/tags">
          <template #icon><TagsOutlined /></template>
          标签管理
        </a-menu-item>
        <a-menu-item key="/reports">
          <template #icon><WarningOutlined /></template>
          举报管理
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="app-header">
        <div class="header-left">
          <MenuUnfoldOutlined v-if="collapsed" class="trigger" @click="collapsed = false" />
          <MenuFoldOutlined v-else class="trigger" @click="collapsed = true" />
          <a-breadcrumb style="margin-left: 16px">
            <a-breadcrumb-item>首页</a-breadcrumb-item>
            <a-breadcrumb-item>{{ currentTitle }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="header-right">
          <a-dropdown>
            <a class="user-action" @click.prevent>
              <a-avatar :size="32" style="background-color: #1677ff">
                {{ username.charAt(0).toUpperCase() }}
              </a-avatar>
              <span style="margin-left: 8px; color: #333">{{ username }}</span>
            </a>
            <template #overlay>
              <a-menu @click="logout">
                <a-menu-item key="logout">
                  <LogoutOutlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content class="app-content">
        <router-view />
      </a-layout-content>
      <a-layout-footer style="text-align: center; color: #999; padding: 16px">
        校园知乎管理后台 ©2026
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  DashboardOutlined, UserOutlined, QuestionCircleOutlined, MessageOutlined,
  CommentOutlined, TagsOutlined, WarningOutlined, LogoutOutlined,
  MenuFoldOutlined, MenuUnfoldOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)

const titleMap = {
  '/dashboard': '数据概览', '/users': '用户管理', '/questions': '问题管理',
  '/answers': '回答管理', '/comments': '评论管理', '/tags': '标签管理', '/reports': '举报管理'
}
const currentTitle = computed(() => titleMap[route.path] || '')
const username = computed(() => localStorage.getItem('admin_username') || '管理员')

const logout = () => {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_username')
  router.push('/login')
}
</script>

<style scoped>
.logo {
  height: 64px; display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 18px; font-weight: 700; letter-spacing: 1px;
  background: rgba(255,255,255,0.05);
}
.app-header {
  background: #fff; padding: 0 24px; display: flex; align-items: center;
  justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,.06);
  height: 64px; line-height: 64px;
}
.header-left { display: flex; align-items: center; }
.header-right { display: flex; align-items: center; }
.trigger { font-size: 20px; cursor: pointer; transition: color .3s; }
.trigger:hover { color: #1677ff; }
.user-action { display: flex; align-items: center; cursor: pointer; }
.app-content { margin: 24px; padding: 24px; background: #fff; border-radius: 8px; min-height: 280px; }
</style>
