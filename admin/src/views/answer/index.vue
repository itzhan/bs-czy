<template>
  <div>
    <div class="page-header">
      <h2>回答管理</h2>
      <a-space>
        <a-select v-model:value="statusFilter" placeholder="状态筛选" style="width: 120px" allowClear @change="loadData">
          <a-select-option :value="1">已发布</a-select-option>
          <a-select-option :value="0">待审核</a-select-option>
          <a-select-option :value="2">已拒绝</a-select-option>
        </a-select>
        <a-button type="primary" @click="openModal()">新增回答</a-button>
      </a-space>
    </div>
    <a-table :dataSource="tableData" :columns="columns" :pagination="pagination" @change="handleTableChange"
      :rowKey="r => r.id" bordered size="middle">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status===1?'green':record.status===0?'blue':'red'">{{ ['待审核','已发布','已拒绝'][record.status] }}</a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="openModal(record)">编辑</a-button>
            <a-button v-if="record.status===0" size="small" type="primary" @click="approve(record.id)">通过</a-button>
            <a-button v-if="record.status===0" size="small" @click="reject(record.id)">拒绝</a-button>
            <a-popconfirm title="确认删除?" @confirm="remove(record.id)">
              <a-button size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="modalVisible" :title="editingId ? '编辑回答' : '新增回答'" @ok="submitForm" :confirmLoading="submitting">
      <a-form :label-col="{ span: 5 }">
        <a-form-item v-if="!editingId" label="问题ID" required>
          <a-input-number v-model:value="form.questionId" placeholder="所属问题ID" style="width: 100%" :min="1" />
        </a-form-item>
        <a-form-item label="内容" required>
          <a-textarea v-model:value="form.content" placeholder="回答内容" :rows="4" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status">
            <a-select-option :value="1">已发布</a-select-option>
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="2">已拒绝</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getAnswers, createAnswer, updateAnswer, updateAnswerStatus, deleteAnswer } from '../../api'
import { message } from 'ant-design-vue'

const tableData = ref([]), statusFilter = ref(undefined), page = ref(1), size = ref(20), total = ref(0)
const modalVisible = ref(false), editingId = ref(null), submitting = ref(false)
const form = reactive({ questionId: null, content: '', status: 1 })

const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '内容', dataIndex: 'content', ellipsis: true },
  { title: '问题ID', dataIndex: 'questionId', width: 90 },
  { title: '状态', key: 'status', width: 90 },
  { title: '点赞', dataIndex: 'likeCount', width: 80 },
  { title: '操作', key: 'action', width: 250, fixed: 'right' },
]
const pagination = computed(() => ({ current: page.value, pageSize: size.value, total: total.value, showTotal: t => `共 ${t} 条` }))

const loadData = async () => {
  try {
    const d = await getAnswers({ page: page.value, size: size.value, status: statusFilter.value })
    tableData.value = d.records; total.value = d.total
  } catch (e) {}
}
const handleTableChange = (p) => { page.value = p.current; loadData() }
const approve = async (id) => { await updateAnswerStatus(id, 1); message.success('已通过'); loadData() }
const reject = async (id) => { await updateAnswerStatus(id, 2); message.success('已拒绝'); loadData() }
const remove = async (id) => { await deleteAnswer(id); message.success('已删除'); loadData() }

const openModal = (record) => {
  editingId.value = record ? record.id : null
  Object.assign(form, { questionId: null, content: '', status: 1 })
  if (record) Object.assign(form, { questionId: record.questionId, content: record.content || '', status: record.status })
  modalVisible.value = true
}

const submitForm = async () => {
  if (!form.content) { message.error('内容必填'); return }
  submitting.value = true
  try {
    if (editingId.value) { await updateAnswer(editingId.value, { content: form.content, status: form.status }) }
    else {
      if (!form.questionId) { message.error('问题ID必填'); submitting.value = false; return }
      await createAnswer(form)
    }
    message.success(editingId.value ? '更新成功' : '创建成功')
    modalVisible.value = false; loadData()
  } catch (e) { message.error('操作失败') }
  finally { submitting.value = false }
}

onMounted(loadData)
</script>
