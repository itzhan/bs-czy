<template>
  <div>
    <div class="page-header">
      <h2>用户管理</h2>
      <a-space>
        <a-input-search v-model:value="keyword" placeholder="搜索用户" style="width: 200px" @search="loadData" allowClear />
        <a-select v-model:value="roleFilter" placeholder="角色筛选" style="width: 120px" allowClear @change="loadData">
          <a-select-option :value="0">学生</a-select-option>
          <a-select-option :value="1">教师</a-select-option>
          <a-select-option :value="2">管理员</a-select-option>
        </a-select>
      </a-space>
    </div>
    <a-table :dataSource="tableData" :columns="columns" :pagination="pagination" @change="handleTableChange"
      :rowKey="r => r.id" bordered size="middle">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'role'">
          <a-tag :color="record.role===2?'red':record.role===1?'orange':'blue'">{{ ['学生','教师','管理员'][record.role] }}</a-tag>
        </template>
        <template v-if="column.key === 'status'">
          <a-badge :status="record.status===1?'success':'error'" :text="record.status===1?'正常':'禁用'" />
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button size="small" :danger="record.status===1" @click="toggleStatus(record)">
              {{ record.status===1?'禁用':'启用' }}
            </a-button>
            <a-select v-model:value="record.role" size="small" style="width: 80px" @change="changeRole(record)">
              <a-select-option :value="0">学生</a-select-option>
              <a-select-option :value="1">教师</a-select-option>
              <a-select-option :value="2">管理员</a-select-option>
            </a-select>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getUsers, updateUserStatus, updateUserRole } from '../../api'
import { message } from 'ant-design-vue'

const tableData = ref([]), keyword = ref(''), roleFilter = ref(undefined), page = ref(1), size = ref(20), total = ref(0)
const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 70 },
  { title: '用户名', dataIndex: 'username', key: 'username', width: 120 },
  { title: '昵称', dataIndex: 'nickname', key: 'nickname', width: 120 },
  { title: '院系', dataIndex: 'department', key: 'department', width: 150 },
  { title: '角色', key: 'role', width: 90 },
  { title: '状态', key: 'status', width: 90 },
  { title: '提问', dataIndex: 'questionCount', key: 'qc', width: 70 },
  { title: '回答', dataIndex: 'answerCount', key: 'ac', width: 70 },
  { title: '粉丝', dataIndex: 'followerCount', key: 'fc', width: 70 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' },
]
const pagination = computed(() => ({ current: page.value, pageSize: size.value, total: total.value, showTotal: t => `共 ${t} 条` }))

const loadData = async () => {
  try {
    const d = await getUsers({ page: page.value, size: size.value, keyword: keyword.value || undefined, role: roleFilter.value })
    tableData.value = d.records; total.value = d.total
  } catch (e) {}
}

const handleTableChange = (p) => { page.value = p.current; loadData() }
const toggleStatus = async (row) => { await updateUserStatus(row.id, row.status === 1 ? 0 : 1); message.success('操作成功'); loadData() }
const changeRole = async (row) => { await updateUserRole(row.id, row.role); message.success('角色已更新') }
onMounted(loadData)
</script>
