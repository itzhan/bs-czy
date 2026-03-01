<template>
  <div>
    <div class="page-header">
      <h2>举报管理</h2>
      <a-select v-model:value="statusFilter" placeholder="状态筛选" style="width: 120px" allowClear @change="loadData">
        <a-select-option :value="0">待处理</a-select-option>
        <a-select-option :value="1">已处理</a-select-option>
        <a-select-option :value="2">已忽略</a-select-option>
      </a-select>
    </div>
    <a-table :dataSource="tableData" :columns="columns" :pagination="pagination" @change="handleTableChange"
      :rowKey="r => r.id" bordered size="middle">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'target'">
          {{ {1:'问题',2:'回答',3:'评论'}[record.targetType] }} #{{ record.targetId }}
        </template>
        <template v-if="column.key === 'reason'">
          <a-tag>{{ {1:'垃圾广告',2:'违规内容',3:'不友善',4:'其他',5:'与主题无关'}[record.reason] }}</a-tag>
        </template>
        <template v-if="column.key === 'status'">
          <a-badge :status="record.status===0?'warning':record.status===1?'success':'default'" :text="['待处理','已处理','已忽略'][record.status]" />
        </template>
        <template v-if="column.key === 'action'">
          <template v-if="record.status===0">
            <a-space>
              <a-button size="small" type="primary" @click="handle(record.id, 1, '')">处理</a-button>
              <a-button size="small" @click="handle(record.id, 2, '')">忽略</a-button>
            </a-space>
          </template>
          <span v-else style="color: #999">已处理</span>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getReports, handleReport } from '../../api'
import { message } from 'ant-design-vue'

const tableData = ref([]), statusFilter = ref(undefined), page = ref(1), size = ref(20), total = ref(0)
const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '目标', key: 'target', width: 120 },
  { title: '举报原因', key: 'reason', width: 110 },
  { title: '描述', dataIndex: 'description', ellipsis: true },
  { title: '状态', key: 'status', width: 100 },
  { title: '处理备注', dataIndex: 'handleNote', ellipsis: true },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
]
const pagination = computed(() => ({ current: page.value, pageSize: size.value, total: total.value, showTotal: t => `共 ${t} 条` }))

const loadData = async () => {
  try {
    const d = await getReports({ page: page.value, size: size.value, status: statusFilter.value })
    tableData.value = d.records; total.value = d.total
  } catch (e) {}
}
const handleTableChange = (p) => { page.value = p.current; loadData() }
const handle = async (id, status, note) => { await handleReport(id, status, note); message.success('操作成功'); loadData() }
onMounted(loadData)
</script>
