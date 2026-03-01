<template>
  <div class="page-container">
    <h2 style="margin-bottom:20px;">数据概览</h2>
    <div class="stat-cards">
      <div class="stat-card">
        <div class="label">用户总数</div>
        <div class="value primary">{{ stats.userCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">问题总数</div>
        <div class="value success">{{ stats.questionCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">回答总数</div>
        <div class="value warning">{{ stats.answerCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">评论总数</div>
        <div class="value">{{ stats.commentCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">待处理举报</div>
        <div class="value danger">{{ stats.pendingReports || 0 }}</div>
      </div>
    </div>
    <el-card>
      <template #header><span>数据趋势</span></template>
      <div ref="chartRef" style="height:350px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStats } from '../../api'
import * as echarts from 'echarts'

const stats = ref({})
const chartRef = ref(null)

onMounted(async () => {
  try { stats.value = await getStats() } catch (e) {}
  // 模拟趋势图
  const chart = echarts.init(chartRef.value)
  const days = ['周一','周二','周三','周四','周五','周六','周日']
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增问题', '新增回答', '新增用户'] },
    xAxis: { type: 'category', data: days },
    yAxis: { type: 'value' },
    series: [
      { name: '新增问题', type: 'line', smooth: true, data: [5,8,12,7,15,9,11], areaStyle: { opacity: 0.1 }, color: '#409eff' },
      { name: '新增回答', type: 'line', smooth: true, data: [12,15,8,20,18,25,22], areaStyle: { opacity: 0.1 }, color: '#67c23a' },
      { name: '新增用户', type: 'line', smooth: true, data: [3,5,2,4,6,3,5], areaStyle: { opacity: 0.1 }, color: '#e6a23c' },
    ]
  })
  window.addEventListener('resize', () => chart.resize())
})
</script>
