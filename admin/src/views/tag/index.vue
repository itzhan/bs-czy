<template>
  <div>
    <div class="page-header">
      <h2>标签管理</h2>
      <a-button type="primary" @click="showModal()">
        <template #icon><PlusOutlined /></template>
        新增标签
      </a-button>
    </div>
    <a-table :dataSource="tableData" :columns="columns" :rowKey="r => r.id" bordered size="middle">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="showModal(record)">编辑</a-button>
            <a-popconfirm title="确认删除?" @confirm="remove(record.id)">
              <a-button size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="modalVisible" :title="editId ? '编辑标签' : '新增标签'" @ok="submit" okText="确定" cancelText="取消">
      <a-form :model="form" layout="vertical">
        <a-form-item label="名称"><a-input v-model:value="form.name" /></a-form-item>
        <a-form-item label="描述"><a-textarea v-model:value="form.description" :rows="3" /></a-form-item>
        <a-form-item label="图标"><a-input v-model:value="form.icon" placeholder="可选" /></a-form-item>
        <a-form-item label="排序"><a-input-number v-model:value="form.sortOrder" :min="0" style="width: 100%" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTags, createTag, updateTag, deleteTag } from '../../api'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'

const tableData = ref([]), modalVisible = ref(false), editId = ref(null)
const form = ref({ name: '', description: '', icon: '', sortOrder: 0 })
const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '名称', dataIndex: 'name', width: 150 },
  { title: '描述', dataIndex: 'description', ellipsis: true },
  { title: '问题数', dataIndex: 'questionCount', width: 90 },
  { title: '排序', dataIndex: 'sortOrder', width: 70 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' },
]

const loadData = async () => { try { tableData.value = await getTags() } catch (e) {} }
const showModal = (row) => {
  if (row) { editId.value = row.id; form.value = { name: row.name, description: row.description||'', icon: row.icon||'', sortOrder: row.sortOrder||0 } }
  else { editId.value = null; form.value = { name: '', description: '', icon: '', sortOrder: 0 } }
  modalVisible.value = true
}
const submit = async () => {
  if (editId.value) await updateTag(editId.value, form.value)
  else await createTag(form.value)
  message.success('操作成功'); modalVisible.value = false; loadData()
}
const remove = async (id) => { await deleteTag(id); message.success('已删除'); loadData() }
onMounted(loadData)
</script>
