<template>
  <div class="page-container">
    <div class="page-header"><h2>回答管理</h2>
      <el-select v-model="statusFilter" placeholder="状态" style="width:120px;" clearable @change="loadData">
        <el-option label="已发布" :value="1" /><el-option label="待审核" :value="0" /><el-option label="已拒绝" :value="2" />
      </el-select>
    </div>
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="questionId" label="问题ID" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status===1?'success':row.status===0?'':'danger'">{{ ['待审核','已发布','已拒绝'][row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="likeCount" label="点赞" width="80" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status===0" size="small" type="success" @click="approve(row.id)">通过</el-button>
          <el-button v-if="row.status===0" size="small" type="warning" @click="reject(row.id)">拒绝</el-button>
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
import { getAnswers, updateAnswerStatus, deleteAnswer } from '../../api'
import { ElMessage } from 'element-plus'
const tableData = ref([]), statusFilter = ref(null), page = ref(1), size = ref(20), total = ref(0)
const loadData = async () => { try { const d = await getAnswers({ page:page.value, size:size.value, status:statusFilter.value??undefined }); tableData.value=d.records; total.value=d.total } catch(e){} }
const approve = async (id) => { await updateAnswerStatus(id, 1); ElMessage.success('已通过'); loadData() }
const reject = async (id) => { await updateAnswerStatus(id, 2); ElMessage.success('已拒绝'); loadData() }
const remove = async (id) => { await deleteAnswer(id); ElMessage.success('已删除'); loadData() }
onMounted(loadData)
</script>
