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
          <a-popconfirm title="确认删除?" @confirm="remove(record.id)">
            <a-button size="small" danger>删除</a-button>
          </a-popconfirm>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getComments, deleteComment } from '../../api'
import { message } from 'ant-design-vue'

const tableData = ref([]), page = ref(1), size = ref(20), total = ref(0)
const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '内容', dataIndex: 'content', ellipsis: true },
  { title: '用户ID', dataIndex: 'userId', width: 80 },
  { title: '目标', key: 'target', width: 120 },
  { title: '点赞', dataIndex: 'likeCount', width: 70 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' },
]
const pagination = computed(() => ({ current: page.value, pageSize: size.value, total: total.value, showTotal: t => `共 ${t} 条` }))

const loadData = async () => {
  try { const d = await getComments({ page: page.value, size: size.value }); tableData.value = d.records; total.value = d.total } catch (e) {}
}
const handleTableChange = (p) => { page.value = p.current; loadData() }
const remove = async (id) => { await deleteComment(id); message.success('已删除'); loadData() }
onMounted(loadData)
</script>
