<template>
  <div>
    <div class="page-header">
      <h2>问题管理</h2>
      <a-space>
        <a-input-search v-model:value="keyword" placeholder="搜索问题" style="width: 200px" @search="loadData" allowClear />
        <a-select v-model:value="statusFilter" placeholder="状态筛选" style="width: 120px" allowClear @change="loadData">
          <a-select-option :value="1">已发布</a-select-option>
          <a-select-option :value="3">待审核</a-select-option>
          <a-select-option :value="2">已关闭</a-select-option>
          <a-select-option :value="4">已拒绝</a-select-option>
        </a-select>
        <a-button type="primary" @click="openModal()">新增问题</a-button>
      </a-space>
    </div>
    <a-table :dataSource="tableData" :columns="columns" :pagination="pagination" @change="handleTableChange"
      :rowKey="r => r.id" bordered size="middle">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusMap[record.status]?.color">{{ statusMap[record.status]?.label }}</a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="openModal(record)">编辑</a-button>
            <a-button v-if="record.status===3" size="small" type="primary" @click="approve(record.id)">通过</a-button>
            <a-button v-if="record.status===3" size="small" @click="reject(record.id)">拒绝</a-button>
            <a-button v-if="record.status===1" size="small" @click="closeQ(record.id)">关闭</a-button>
            <a-popconfirm title="确认删除?" @confirm="remove(record.id)">
              <a-button size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="modalVisible" :title="editingId ? '编辑问题' : '新增问题'" @ok="submitForm" :confirmLoading="submitting">
      <a-form :label-col="{ span: 4 }">
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" placeholder="问题标题" />
        </a-form-item>
        <a-form-item label="内容" required>
          <a-textarea v-model:value="form.content" placeholder="问题内容" :rows="4" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status">
            <a-select-option :value="1">已发布</a-select-option>
            <a-select-option :value="3">待审核</a-select-option>
            <a-select-option :value="2">已关闭</a-select-option>
            <a-select-option :value="4">已拒绝</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getQuestions, createQuestion, updateQuestion, updateQuestionStatus, deleteQuestion } from '../../api'
import { message } from 'ant-design-vue'

const statusMap = { 0:{label:'草稿',color:'default'}, 1:{label:'已发布',color:'green'}, 2:{label:'已关闭',color:'orange'}, 3:{label:'待审核',color:'blue'}, 4:{label:'已拒绝',color:'red'} }
const tableData = ref([]), keyword = ref(''), statusFilter = ref(undefined), page = ref(1), size = ref(20), total = ref(0)
const modalVisible = ref(false), editingId = ref(null), submitting = ref(false)
const form = reactive({ title: '', content: '', status: 1 })

const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '标题', dataIndex: 'title', ellipsis: true },
  { title: '状态', key: 'status', width: 90 },
  { title: '浏览', dataIndex: 'viewCount', width: 80 },
  { title: '回答', dataIndex: 'answerCount', width: 80 },
  { title: '点赞', dataIndex: 'likeCount', width: 80 },
  { title: '操作', key: 'action', width: 280, fixed: 'right' },
]
const pagination = computed(() => ({ current: page.value, pageSize: size.value, total: total.value, showTotal: t => `共 ${t} 条` }))

const loadData = async () => {
  try {
    const d = await getQuestions({ page: page.value, size: size.value, keyword: keyword.value || undefined, status: statusFilter.value })
    tableData.value = d.records; total.value = d.total
  } catch (e) {}
}
const handleTableChange = (p) => { page.value = p.current; loadData() }
const approve = async (id) => { await updateQuestionStatus(id, 1); message.success('已通过'); loadData() }
const reject = async (id) => { await updateQuestionStatus(id, 4); message.success('已拒绝'); loadData() }
const closeQ = async (id) => { await updateQuestionStatus(id, 2); message.success('已关闭'); loadData() }
const remove = async (id) => { await deleteQuestion(id); message.success('已删除'); loadData() }

const openModal = (record) => {
  editingId.value = record ? record.id : null
  Object.assign(form, { title: '', content: '', status: 1 })
  if (record) Object.assign(form, { title: record.title, content: record.content || '', status: record.status })
  modalVisible.value = true
}

const submitForm = async () => {
  if (!form.title || !form.content) { message.error('标题和内容必填'); return }
  submitting.value = true
  try {
    if (editingId.value) { await updateQuestion(editingId.value, form) }
    else { await createQuestion(form) }
    message.success(editingId.value ? '更新成功' : '创建成功')
    modalVisible.value = false; loadData()
  } catch (e) { message.error('操作失败') }
  finally { submitting.value = false }
}

onMounted(loadData)
</script>
