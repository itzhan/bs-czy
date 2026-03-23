<template>
  <div>
    <div class="page-header"><h2>评论管理</h2></div>
    <a-table :dataSource="tableData" :columns="columns" :pagination="pagination" @change="handleTableChange"
      :rowKey="r => r.id" bordered size="middle">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'target'">
          <a-tag :color="record.targetType===1?'blue':'green'">{{ record.targetType===1?'问题':'回答' }}</a-tag>
          #{{ record.targetId }}
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="openModal(record)">编辑</a-button>
            <a-popconfirm title="确认删除?" @confirm="remove(record.id)">
              <a-button size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="modalVisible" title="编辑评论" @ok="submitForm" :confirmLoading="submitting">
      <a-form :label-col="{ span: 4 }">
        <a-form-item label="内容" required>
          <a-textarea v-model:value="form.content" placeholder="评论内容" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getComments, updateComment, deleteComment } from '../../api'
import { message } from 'ant-design-vue'

const tableData = ref([]), page = ref(1), size = ref(20), total = ref(0)
const modalVisible = ref(false), editingId = ref(null), submitting = ref(false)
const form = reactive({ content: '' })

const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '内容', dataIndex: 'content', ellipsis: true },
  { title: '用户ID', dataIndex: 'userId', width: 80 },
  { title: '目标', key: 'target', width: 120 },
  { title: '点赞', dataIndex: 'likeCount', width: 70 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
]
const pagination = computed(() => ({ current: page.value, pageSize: size.value, total: total.value, showTotal: t => `共 ${t} 条` }))

const loadData = async () => {
  try { const d = await getComments({ page: page.value, size: size.value }); tableData.value = d.records; total.value = d.total } catch (e) {}
}
const handleTableChange = (p) => { page.value = p.current; loadData() }
const remove = async (id) => { await deleteComment(id); message.success('已删除'); loadData() }

const openModal = (record) => {
  editingId.value = record.id
  form.content = record.content || ''
  modalVisible.value = true
}

const submitForm = async () => {
  if (!form.content) { message.error('内容必填'); return }
  submitting.value = true
  try {
    await updateComment(editingId.value, { content: form.content })
    message.success('更新成功')
    modalVisible.value = false; loadData()
  } catch (e) { message.error('操作失败') }
  finally { submitting.value = false }
}

onMounted(loadData)
</script>
