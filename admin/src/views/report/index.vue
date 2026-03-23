<template>
  <div>
    <div class="page-header">
      <h2>举报管理</h2>
      <a-select v-model:value="statusFilter" placeholder="状态筛选" style="width: 120px" allowClear @change="loadData">
        <a-select-option :value="0">待处理</a-select-option>
        <a-select-option :value="1">已处理</a-select-option>
        <a-select-option :value="2">已驳回</a-select-option>
      </a-select>
    </div>
    <a-table :dataSource="tableData" :columns="columns" :pagination="pagination" @change="handleTableChange"
      :rowKey="r => r.id" bordered size="middle">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'target'">
          {{ {1:'问题',2:'回答',3:'评论'}[record.targetType] }} #{{ record.targetId }}
        </template>
        <template v-if="column.key === 'reason'">
          <a-tag>{{ {1:'垃圾广告',2:'色情低俗',3:'不实信息',4:'人身攻击',5:'违法违规',6:'其他'}[record.reason] }}</a-tag>
        </template>
        <template v-if="column.key === 'status'">
          <a-badge :status="record.status===0?'warning':record.status===1?'success':'default'"
            :text="['待处理','已处理(删除)','已驳回'][record.status]" />
        </template>
        <template v-if="column.key === 'action'">
          <a-button v-if="record.status===0" size="small" type="primary" @click="openProcessModal(record)">处理</a-button>
          <span v-else style="color:#999">{{ record.status===1?'已删除内容':'已驳回' }}</span>
        </template>
      </template>
    </a-table>

    <!-- 处理弹窗：内容详情 + 操作 -->
    <a-modal v-model:open="processModalVisible" title="举报处理" :footer="null" width="650px" :destroyOnClose="true">
      <a-spin :spinning="contentLoading">
        <template v-if="contentData">
          <!-- 举报信息 -->
          <a-descriptions title="举报信息" :column="2" bordered size="small" style="margin-bottom: 16px">
            <a-descriptions-item label="举报人">{{ contentData.reporterName }}</a-descriptions-item>
            <a-descriptions-item label="举报原因"><a-tag color="red">{{ contentData.reasonLabel }}</a-tag></a-descriptions-item>
            <a-descriptions-item label="补充说明" :span="2">{{ contentData.description || '无' }}</a-descriptions-item>
          </a-descriptions>

          <!-- 被举报内容 -->
          <a-descriptions title="被举报内容" :column="2" bordered size="small" style="margin-bottom: 16px">
            <a-descriptions-item label="类型">{{ {1:'问题',2:'回答'}[contentData.targetType] }} #{{ contentData.targetId }}</a-descriptions-item>
            <a-descriptions-item label="作者">{{ contentData.authorName || '未知' }}</a-descriptions-item>
            <a-descriptions-item v-if="contentData.title" label="标题" :span="2"><strong>{{ contentData.title }}</strong></a-descriptions-item>
            <a-descriptions-item label="正文" :span="2">
              <div style="max-height: 200px; overflow-y: auto; white-space: pre-wrap; line-height: 1.6; background: #fafafa; padding: 8px; border-radius: 4px">{{ contentData.content }}</div>
            </a-descriptions-item>
          </a-descriptions>

          <!-- 操作区 -->
          <a-divider />
          <a-form :label-col="{ span: 4 }">
            <a-form-item label="备注/理由">
              <a-textarea v-model:value="handleNote" placeholder="填写处理备注或驳回理由" :rows="2" />
            </a-form-item>
          </a-form>
          <div style="display: flex; justify-content: flex-end; gap: 12px">
            <a-button @click="doHandle(2)" :loading="submitting">举报失败（驳回）</a-button>
            <a-button type="primary" danger @click="doHandle(1)" :loading="submitting">删除内容并通知</a-button>
          </div>
        </template>
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getReports, getReportContent, handleReport } from '../../api'
import { message } from 'ant-design-vue'

const tableData = ref([]), statusFilter = ref(undefined), page = ref(1), size = ref(20), total = ref(0)
const processModalVisible = ref(false), contentData = ref(null), contentLoading = ref(false)
const handleNote = ref(''), currentReportId = ref(null), submitting = ref(false)

const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '目标', key: 'target', width: 120 },
  { title: '举报原因', key: 'reason', width: 110 },
  { title: '描述', dataIndex: 'description', ellipsis: true },
  { title: '状态', key: 'status', width: 120 },
  { title: '处理备注', dataIndex: 'handleNote', ellipsis: true },
  { title: '操作', key: 'action', width: 100, fixed: 'right' },
]
const pagination = computed(() => ({ current: page.value, pageSize: size.value, total: total.value, showTotal: t => `共 ${t} 条` }))

const loadData = async () => {
  try {
    const d = await getReports({ page: page.value, size: size.value, status: statusFilter.value })
    tableData.value = d.records; total.value = d.total
  } catch (e) {}
}
const handleTableChange = (p) => { page.value = p.current; loadData() }

const openProcessModal = async (record) => {
  currentReportId.value = record.id
  handleNote.value = ''
  contentData.value = null
  contentLoading.value = true
  processModalVisible.value = true
  try {
    contentData.value = await getReportContent(record.id)
  } catch (e) { message.error('加载内容失败') }
  finally { contentLoading.value = false }
}

const doHandle = async (status) => {
  submitting.value = true
  try {
    await handleReport(currentReportId.value, status, handleNote.value || undefined)
    message.success(status === 1 ? '已删除内容并通知双方' : '已驳回并通知举报人')
    processModalVisible.value = false; loadData()
  } catch (e) { message.error('操作失败') }
  finally { submitting.value = false }
}

onMounted(loadData)
</script>
