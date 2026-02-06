<template>
  <div class="achievement">
    <div class="page-card">
      <div class="page-header">
        <span class="page-title">成果登记</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增成果
        </el-button>
      </div>
      
      <div class="search-bar">
        <el-select v-model="query.type" placeholder="成果类型" clearable style="width: 140px">
          <el-option label="论文" :value="1" />
          <el-option label="专利" :value="2" />
          <el-option label="软著" :value="3" />
        </el-select>
        <el-input 
          v-model="query.keyword" 
          placeholder="搜索成果名称/作者" 
          clearable 
          style="width: 260px"
          @keyup.enter="loadData"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadData">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
      
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="type" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small" effect="light">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="成果名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="title-link" @click="handleView(row)">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="authors" label="作者/发明人" width="140" show-overflow-tooltip />
        <el-table-column prop="publicationVenue" label="发表刊物/授权单位" width="160" show-overflow-tooltip />
        <el-table-column prop="publicationDate" label="日期" width="110" align="center" />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small" effect="light">
              {{ row.status === 1 ? '已发表/授权' : '申请中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="handleView(row)">查看</el-button>
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-popconfirm title="确定删除该成果吗？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button link type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination 
          v-model:current-page="query.page" 
          v-model:page-size="query.size" 
          :total="total" 
          :page-sizes="[10, 20, 50]" 
          layout="total, sizes, prev, pager, next, jumper" 
          background 
          @change="loadData" 
        />
      </div>
    </div>
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px" :disabled="isView" class="dialog-form">
        <!-- 基本信息区域 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            <span>基本信息</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="成果类型" prop="type">
                <el-select v-model="form.type" style="width: 100%">
                  <el-option label="论文" :value="1" />
                  <el-option label="专利" :value="2" />
                  <el-option label="软著" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="状态">
                <el-select v-model="form.status" style="width: 100%">
                  <el-option label="已发表/授权" :value="1" />
                  <el-option label="申请中" :value="2" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="成果名称" prop="title">
            <el-input v-model="form.title" placeholder="请输入成果名称" />
          </el-form-item>
          <el-form-item label="作者/发明人">
            <el-input v-model="form.authors" placeholder="多人用逗号分隔" />
          </el-form-item>
        </div>
        
        <!-- 发表信息区域 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Calendar /></el-icon>
            <span>发表信息</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="发表刊物/授权单位">
                <el-input v-model="form.publicationVenue" placeholder="请输入" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="发表/授权日期">
                <el-date-picker v-model="form.publicationDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="DOI/专利号/登记号">
            <el-input v-model="form.doiOrNumber" placeholder="请输入编号" />
          </el-form-item>
        </div>
        
        <!-- 详细描述区域 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><EditPen /></el-icon>
            <span>详细描述</span>
          </div>
          <el-form-item label="描述">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="成果描述" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer v-if="!isView">
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { achievementApi } from '@/api/achievement'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isView = ref(false)
const editId = ref(null)
const tableData = ref([])
const total = ref(0)
const formRef = ref()

const query = reactive({ page: 1, size: 10, type: null, keyword: '' })

const form = reactive({
  type: 1,
  title: '',
  authors: '',
  publicationVenue: '',
  publicationDate: '',
  doiOrNumber: '',
  attachmentUrl: '',
  description: '',
  status: 1
})

const rules = {
  type: [{ required: true, message: '请选择成果类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入成果名称', trigger: 'blur' }]
}

const dialogTitle = computed(() => isView.value ? '查看详情' : (editId.value ? '编辑成果' : '新增成果'))

const typeText = (type) => ({ 1: '论文', 2: '专利', 3: '软著' }[type])
const typeTagType = (type) => ({ 1: 'primary', 2: 'success', 3: 'warning' }[type])

const loadData = async () => {
  loading.value = true
  try {
    const res = await achievementApi.list(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.type = null
  query.keyword = ''
  query.page = 1
  loadData()
}

const resetForm = () => {
  form.type = 1
  form.title = ''
  form.authors = ''
  form.publicationVenue = ''
  form.publicationDate = ''
  form.doiOrNumber = ''
  form.attachmentUrl = ''
  form.description = ''
  form.status = 1
  editId.value = null
  isView.value = false
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleView = async (row) => {
  resetForm()
  isView.value = true
  const res = await achievementApi.getById(row.id)
  Object.assign(form, res.data)
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  editId.value = row.id
  const res = await achievementApi.getById(row.id)
  Object.assign(form, res.data)
  dialogVisible.value = true
}

const handleDelete = async (id) => {
  await achievementApi.delete(id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (editId.value) {
      await achievementApi.update(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await achievementApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

loadData()
</script>

<style lang="scss" scoped>
.title-link {
  color: #4080FF;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
  
  &:hover {
    color: #6AA1FF;
    text-decoration: underline;
  }
}

// 弹框表单样式
.dialog-form {
  .form-section {
    margin-bottom: 24px;
    padding: 20px;
    background: #F7F8FA;
    border-radius: 12px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: #1D2129;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #E5E6EB;
    
    .el-icon {
      color: #4080FF;
      font-size: 18px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
