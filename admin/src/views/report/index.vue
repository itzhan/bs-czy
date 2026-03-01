<template>
  <div class="page-container">
    <div class="page-header"><h2>举报管理</h2>
      <el-select v-model="statusFilter" placeholder="状态" style="width:120px;" clearable @change="loadData">
        <el-option label="待处理" :value="0" /><el-option label="已处理" :value="1" /><el-option label="已忽略" :value="2" />
      </el-select>
    </div>
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="目标" width="120">
        <template #default="{ row }">
          {{ {1:'问题',2:'回答',3:'评论'}[row.targetType] }} #{{ row.targetId }}
        </template>
      </el-table-column>
      <el-table-column label="举报原因" width="120">
        <template #default="{ row }">
          {{ {1:'垃圾广告',2:'违规内容',3:'不友善',4:'其他'}[row.reason] }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status===0?'warning':row.status===1?'success':'info'">{{ ['待处理','已处理','已忽略'][row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handleNote" label="处理备注" show-overflow-tooltip />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status===0">
            <el-button size="small" type="success" @click="handle(row.id, 1, '')">处理</el-button>
            <el-button size="small" type="info" @click="handle(row.id, 2, '')">忽略</el-button>
          </template>
          <span v-else style="color:#999;">已处理</span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-size="size" :total="total" @current-change="p=>{page=p;loadData()}" layout="total, prev, pager, next" style="margin-top:16px;justify-content:flex-end;" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getReports, handleReport } from '../../api'
import { ElMessage } from 'element-plus'
const tableData = ref([]), statusFilter = ref(null), page = ref(1), size = ref(20), total = ref(0)
const loadData = async () => { try { const d = await getReports({ page:page.value, size:size.value, status:statusFilter.value??undefined }); tableData.value=d.records; total.value=d.total } catch(e){} }
const handle = async (id, status, note) => { await handleReport(id, status, note); ElMessage.success('操作成功'); loadData() }
onMounted(loadData)
</script>
