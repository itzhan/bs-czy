<template>
  <div class="page-container">
    <div class="page-header"><h2>评论管理</h2></div>
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column label="目标" width="120">
        <template #default="{ row }">
          {{ row.targetType===1?'问题':'回答' }} #{{ row.targetId }}
        </template>
      </el-table-column>
      <el-table-column prop="likeCount" label="点赞" width="80" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
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
import { getComments, deleteComment } from '../../api'
import { ElMessage } from 'element-plus'
const tableData = ref([]), page = ref(1), size = ref(20), total = ref(0)
const loadData = async () => { try { const d = await getComments({ page:page.value, size:size.value }); tableData.value=d.records; total.value=d.total } catch(e){} }
const remove = async (id) => { await deleteComment(id); ElMessage.success('已删除'); loadData() }
onMounted(loadData)
</script>
