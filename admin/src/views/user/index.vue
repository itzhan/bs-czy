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
        <a-button type="primary" @click="openModal()">新增用户</a-button>
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
            <a-button size="small" @click="openModal(record)">编辑</a-button>
            <a-button size="small" :danger="record.status===1" @click="toggleStatus(record)">
              {{ record.status===1?'禁用':'启用' }}
            </a-button>
            <a-popconfirm title="确认删除该用户?" @confirm="remove(record.id)">
              <a-button size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:open="modalVisible" :title="editingId ? '编辑用户' : '新增用户'" @ok="submitForm" :confirmLoading="submitting">
      <a-form :label-col="{ span: 5 }">
        <a-form-item label="用户名" :required="!editingId">
          <a-input v-model:value="form.username" :disabled="!!editingId" placeholder="登录用户名" />
        </a-form-item>
        <a-form-item v-if="!editingId" label="密码" required>
          <a-input-password v-model:value="form.password" placeholder="登录密码" />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model:value="form.nickname" placeholder="显示昵称" />
        </a-form-item>
        <a-form-item label="院系">
          <a-input v-model:value="form.department" placeholder="所属院系" />
        </a-form-item>
        <a-form-item label="简介">
          <a-textarea v-model:value="form.bio" placeholder="个人简介" :rows="2" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="form.role">
            <a-select-option :value="0">学生</a-select-option>
            <a-select-option :value="1">教师</a-select-option>
            <a-select-option :value="2">管理员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status">
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getUsers, createUser, updateUser, updateUserStatus, deleteUser } from '../../api'
import { message } from 'ant-design-vue'

const tableData = ref([]), keyword = ref(''), roleFilter = ref(undefined), page = ref(1), size = ref(20), total = ref(0)
const modalVisible = ref(false), editingId = ref(null), submitting = ref(false)
const form = reactive({ username: '', password: '', nickname: '', department: '', bio: '', role: 0, status: 1 })

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 70 },
  { title: '用户名', dataIndex: 'username', key: 'username', width: 120 },
  { title: '昵称', dataIndex: 'nickname', key: 'nickname', width: 120 },
  { title: '院系', dataIndex: 'department', key: 'department', width: 150 },
  { title: '角色', key: 'role', width: 90 },
  { title: '状态', key: 'status', width: 90 },
  { title: '提问', dataIndex: 'questionCount', key: 'qc', width: 70 },
  { title: '回答', dataIndex: 'answerCount', key: 'ac', width: 70 },
  { title: '操作', key: 'action', width: 230, fixed: 'right' },
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

const openModal = (record) => {
  editingId.value = record ? record.id : null
  Object.assign(form, { username: '', password: '', nickname: '', department: '', bio: '', role: 0, status: 1 })
  if (record) {
    Object.assign(form, { username: record.username, nickname: record.nickname || '', department: record.department || '', bio: record.bio || '', role: record.role, status: record.status })
  }
  modalVisible.value = true
}

const submitForm = async () => {
  submitting.value = true
  try {
    if (editingId.value) {
      await updateUser(editingId.value, { nickname: form.nickname, department: form.department, bio: form.bio, role: form.role, status: form.status })
    } else {
      if (!form.username || !form.password) { message.error('用户名和密码必填'); return }
      await createUser(form)
    }
    message.success(editingId.value ? '更新成功' : '创建成功')
    modalVisible.value = false; loadData()
  } catch (e) { message.error('操作失败') }
  finally { submitting.value = false }
}

const remove = async (id) => { await deleteUser(id); message.success('已删除'); loadData() }

onMounted(loadData)
</script>
