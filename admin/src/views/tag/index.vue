<template>
  <div class="page-container">
    <div class="page-header"><h2>标签管理</h2>
      <el-button type="primary" @click="showDialog()">新增标签</el-button>
    </div>
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="questionCount" label="问题数" width="100" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-popconfirm title="确认删除?" @confirm="remove(row.id)">
            <template #reference><el-button size="small" type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editId?'编辑标签':'新增标签'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="可选" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTags, createTag, updateTag, deleteTag } from '../../api'
import { ElMessage } from 'element-plus'
const tableData = ref([]), dialogVisible = ref(false), editId = ref(null)
const form = ref({ name: '', description: '', icon: '', sortOrder: 0 })

const loadData = async () => { try { tableData.value = await getTags() } catch(e){} }

const showDialog = (row) => {
  if (row) { editId.value = row.id; form.value = { name: row.name, description: row.description||'', icon: row.icon||'', sortOrder: row.sortOrder||0 } }
  else { editId.value = null; form.value = { name: '', description: '', icon: '', sortOrder: 0 } }
  dialogVisible.value = true
}

const submit = async () => {
  if (editId.value) await updateTag(editId.value, form.value)
  else await createTag(form.value)
  ElMessage.success('操作成功'); dialogVisible.value = false; loadData()
}

const remove = async (id) => { await deleteTag(id); ElMessage.success('已删除'); loadData() }
onMounted(loadData)
</script>
