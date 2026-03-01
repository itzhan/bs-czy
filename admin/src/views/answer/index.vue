<template>
  <div>
    <div class="page-header">
      <h2>回答管理</h2>
      <a-select v-model:value="statusFilter" placeholder="状态筛选" style="width: 120px" allowClear @change="loadData">
        <a-select-option :value="1">已发布</a-select-option>
        <a-select-option :value="0">待审核</a-select-option>
        <a-select-option :value="2">已拒绝</a-select-option>
      </a-select>
    </div>
    <a-table :dataSource="tableData" :columns="columns" :pagination="pagination" @change="handleTableChange"
      :rowKey="r => r.id" bordered size="middle">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status===1?'green':record.status===0?'blue':'red'">{{ ['待审核','已发布','已拒绝'][record.status] }}</a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button v-if="record.status===0" size="small" type="primary" @click="approve(record.id)">通过</a-button>
            <a-button v-if="record.status===0" size="small" @click="reject(record.id)">拒绝</a-button>
            <a-popconfirm title="确认删除?" @confirm="remove(record.id)">
              <a-button size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getAnswers, updateAnswerStatus, deleteAnswer } from '../../api'
import { message } from 'ant-design-vue'

const tableData = ref([]), statusFilter = ref(undefined), page = ref(1), size = ref(20), total = ref(0)
const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '内容', dataIndex: 'content', ellipsis: true },
  { title: '问题ID', dataIndex: 'questionId', width: 90 },
  { title: '状态', key: 'status', width: 90 },
  { title: '点赞', dataIndex: 'likeCount', width: 80 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' },
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
onMounted(loadData)
</script>
