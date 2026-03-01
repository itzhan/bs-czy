<template>
  <div class="page-container">
    <div class="page-header">
      <h2>问题管理</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索问题" style="width:200px;margin-right:12px;" clearable @keyup.enter="loadData" />
        <el-select v-model="statusFilter" placeholder="状态" style="width:120px;margin-right:12px;" clearable @change="loadData">
          <el-option label="已发布" :value="1" /><el-option label="待审核" :value="3" /><el-option label="已关闭" :value="2" /><el-option label="已拒绝" :value="4" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column prop="answerCount" label="回答" width="80" />
      <el-table-column prop="likeCount" label="点赞" width="80" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status===3" size="small" type="success" @click="approve(row.id)">通过</el-button>
          <el-button v-if="row.status===3" size="small" type="warning" @click="reject(row.id)">拒绝</el-button>
          <el-button v-if="row.status===1" size="small" type="info" @click="closeQ(row.id)">关闭</el-button>
          <el-popconfirm title="确认删除?" @confirm="remove(row.id)">
            <template #reference><el-button size="small" type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-size="size" :total="total" @current-change="p=>{page=p;loadData()}" layout="total, prev, pager, next" style="margin-top:16px;justify-content:flex-end;" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getQuestions, updateQuestionStatus, deleteQuestion } from '../../api'
import { ElMessage } from 'element-plus'

const statusMap = { 0:{label:'草稿',type:'info'}, 1:{label:'已发布',type:'success'}, 2:{label:'已关闭',type:'warning'}, 3:{label:'待审核',type:''}, 4:{label:'已拒绝',type:'danger'} }
const tableData = ref([]), keyword = ref(''), statusFilter = ref(null), page = ref(1), size = ref(20), total = ref(0)

const loadData = async () => {
  try {
    const data = await getQuestions({ page: page.value, size: size.value, keyword: keyword.value || undefined, status: statusFilter.value ?? undefined })
    tableData.value = data.records; total.value = data.total
  } catch (e) {}
}

const approve = async (id) => { await updateQuestionStatus(id, 1); ElMessage.success('已通过'); loadData() }
const reject = async (id) => { await updateQuestionStatus(id, 4); ElMessage.success('已拒绝'); loadData() }
const closeQ = async (id) => { await updateQuestionStatus(id, 2); ElMessage.success('已关闭'); loadData() }
const remove = async (id) => { await deleteQuestion(id); ElMessage.success('已删除'); loadData() }

onMounted(loadData)
</script>
