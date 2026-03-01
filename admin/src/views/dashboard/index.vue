<template>
  <div>
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :sm="12" :lg="6" v-for="item in statCards" :key="item.label">
        <a-card hoverable>
          <a-statistic :title="item.label" :value="item.value" :value-style="{ color: item.color, fontWeight: 700 }">
            <template #prefix><component :is="item.icon" /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>
    <a-card title="数据趋势" style="margin-top: 24px">
      <div ref="chartRef" style="height: 380px"></div>
    </a-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStats } from '../../api'
import * as echarts from 'echarts'
import { UserOutlined, QuestionCircleOutlined, MessageOutlined, WarningOutlined } from '@ant-design/icons-vue'

const stats = ref({})
const chartRef = ref(null)

const statCards = computed(() => [
  { label: '用户总数', value: stats.value.userCount || 0, color: '#1677ff', icon: UserOutlined },
  { label: '问题总数', value: stats.value.questionCount || 0, color: '#52c41a', icon: QuestionCircleOutlined },
  { label: '回答总数', value: stats.value.answerCount || 0, color: '#fa8c16', icon: MessageOutlined },
  { label: '待处理举报', value: stats.value.pendingReports || 0, color: '#ff4d4f', icon: WarningOutlined },
])

onMounted(async () => {
  try { stats.value = await getStats() } catch (e) {}
  const chart = echarts.init(chartRef.value)
  const days = ['周一','周二','周三','周四','周五','周六','周日']
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增问题', '新增回答', '新增用户'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: days, boundaryGap: false },
    yAxis: { type: 'value' },
    series: [
      { name: '新增问题', type: 'line', smooth: true, data: [5,8,12,7,15,9,11], areaStyle: { opacity: 0.15 }, color: '#1677ff' },
      { name: '新增回答', type: 'line', smooth: true, data: [12,15,8,20,18,25,22], areaStyle: { opacity: 0.15 }, color: '#52c41a' },
      { name: '新增用户', type: 'line', smooth: true, data: [3,5,2,4,6,3,5], areaStyle: { opacity: 0.15 }, color: '#fa8c16' },
    ]
  })
  window.addEventListener('resize', () => chart.resize())
})
</script>
