<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索用户" style="width:200px;margin-right:12px;" clearable @clear="loadData" @keyup.enter="loadData" />
        <el-select v-model="roleFilter" placeholder="角色筛选" style="width:120px;margin-right:12px;" clearable @change="loadData">
          <el-option label="学生" :value="0" /><el-option label="教师" :value="1" /><el-option label="管理员" :value="2" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="department" label="院系" width="150" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role===2?'danger':row.role===1?'warning':'info'">{{ ['学生','教师','管理员'][row.role] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status===1?'success':'danger'">{{ row.status===1?'正常':'禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="questionCount" label="提问" width="70" />
      <el-table-column prop="answerCount" label="回答" width="70" />
      <el-table-column prop="followerCount" label="粉丝" width="70" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" :type="row.status===1?'danger':'success'" @click="toggleStatus(row)">{{ row.status===1?'禁用':'启用' }}</el-button>
          <el-select v-model="row.role" size="small" style="width:90px;" @change="changeRole(row)">
            <el-option label="学生" :value="0" /><el-option label="教师" :value="1" /><el-option label="管理员" :value="2" />
          </el-select>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-size="size" :total="total" @current-change="p => { page=p; loadData() }" layout="total, prev, pager, next" style="margin-top:16px;justify-content:flex-end;" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUsers, updateUserStatus, updateUserRole } from '../../api'
import { ElMessage } from 'element-plus'

const tableData = ref([]), keyword = ref(''), roleFilter = ref(null), page = ref(1), size = ref(20), total = ref(0)

const loadData = async () => {
  try {
    const data = await getUsers({ page: page.value, size: size.value, keyword: keyword.value || undefined, role: roleFilter.value ?? undefined })
    tableData.value = data.records; total.value = data.total
  } catch (e) {}
}

const toggleStatus = async (row) => {
  await updateUserStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('操作成功'); loadData()
}

const changeRole = async (row) => {
  await updateUserRole(row.id, row.role)
  ElMessage.success('角色已更新')
}

onMounted(loadData)
</script>
