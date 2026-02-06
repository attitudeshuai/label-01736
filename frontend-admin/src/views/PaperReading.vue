<template>
  <div class="paper-reading">
    <div class="page-card">
      <div class="page-header">
        <span class="page-title">论文阅读记录</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增记录
        </el-button>
      </div>
      
      <div class="search-bar">
        <el-input 
          v-model="query.keyword" 
          placeholder="搜索论文题目/关键字" 
          clearable 
          style="width: 300px"
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
        <el-table-column prop="title" label="论文题目" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="title-link" @click="handleView(row)">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="authors" label="作者" width="140" show-overflow-tooltip />
        <el-table-column prop="source" label="来源" width="140" show-overflow-tooltip />
        <el-table-column prop="readingDate" label="阅读日期" width="110" align="center" />
        <el-table-column prop="readerName" label="阅读人" width="90" align="center" />
        <el-table-column label="附件" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.attachmentUrl" class="attachment-icon" @click="downloadFile(row.attachmentUrl)">
              <Paperclip />
            </el-icon>
            <span v-else class="no-attachment">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="handleView(row)">查看</el-button>
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-popconfirm title="确定删除该记录吗？" @confirm="handleDelete(row.id)">
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
    
    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" destroy-on-close top="5vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" :disabled="isView" class="dialog-form">
        <!-- 论文基本信息区域 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            <span>论文基本信息</span>
          </div>
          <el-form-item label="论文题目" prop="title">
            <el-input v-model="form.title" placeholder="请输入论文题目" />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="作者" prop="authors">
                <el-input v-model="form.authors" placeholder="多个作者用逗号分隔" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="第一单位">
                <el-input v-model="form.firstInstitution" placeholder="请输入第一单位" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="来源">
                <el-input v-model="form.source" placeholder="期刊/会议名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="关键字">
                <el-input v-model="form.keywords" placeholder="多个关键字用逗号分隔" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        
        <!-- 阅读记录区域 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><EditPen /></el-icon>
            <span>阅读记录</span>
          </div>
          <el-form-item label="阅读日期" prop="readingDate">
            <el-date-picker v-model="form.readingDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
          <el-form-item label="论文工作介绍">
            <el-input v-model="form.workIntroduction" type="textarea" :rows="3" placeholder="简要介绍论文的主要工作" />
          </el-form-item>
          <el-form-item label="创新点/贡献">
            <el-input v-model="form.innovationPoints" type="textarea" :rows="3" placeholder="本文最大的创新点或贡献是什么" />
          </el-form-item>
          <el-form-item label="思考/缺点">
            <el-input v-model="form.thoughtsOrDrawbacks" type="textarea" :rows="3" placeholder="你的思考或本文有哪些缺点" />
          </el-form-item>
        </div>
        
        <!-- 附件区域 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Paperclip /></el-icon>
            <span>论文原文附件</span>
          </div>
          <el-form-item label="论文附件">
            <div class="upload-area">
              <el-upload 
                v-if="!isView"
                :action="uploadUrl" 
                :headers="uploadHeaders" 
                :on-success="handleUploadSuccess" 
                :show-file-list="false" 
                accept=".pdf,.doc,.docx"
              >
                <el-button type="primary" plain>
                  <el-icon><Upload /></el-icon>上传文件
                </el-button>
              </el-upload>
              <a v-if="form.attachmentUrl" :href="form.attachmentUrl" target="_blank" class="attachment-link">
                <el-icon><Document /></el-icon>查看已上传附件
              </a>
              <span v-if="isView && !form.attachmentUrl" class="no-attachment-text">暂无附件</span>
            </div>
            <div v-if="!isView" class="upload-tip">支持 PDF、Word 格式</div>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { paperReadingApi } from '@/api/paperReading'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isView = ref(false)
const editId = ref(null)
const tableData = ref([])
const total = ref(0)
const formRef = ref()

const query = reactive({ page: 1, size: 10, keyword: '' })

const form = reactive({
  readingDate: '',
  title: '',
  keywords: '',
  source: '',
  authors: '',
  firstInstitution: '',
  workIntroduction: '',
  innovationPoints: '',
  thoughtsOrDrawbacks: '',
  attachmentUrl: ''
})

const rules = {
  title: [{ required: true, message: '请输入论文题目', trigger: 'blur' }],
  readingDate: [{ required: true, message: '请选择阅读日期', trigger: 'change' }]
}

const dialogTitle = computed(() => isView.value ? '查看详情' : (editId.value ? '编辑记录' : '新增记录'))

const uploadUrl = '/api/files/upload'
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${localStorage.getItem('token')}` }))

const loadData = async () => {
  loading.value = true
  try {
    const res = await paperReadingApi.list(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.page = 1
  loadData()
}

const resetForm = () => {
  Object.keys(form).forEach(key => form[key] = '')
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
  const res = await paperReadingApi.getById(row.id)
  Object.assign(form, res.data)
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  editId.value = row.id
  const res = await paperReadingApi.getById(row.id)
  Object.assign(form, res.data)
  dialogVisible.value = true
}

const handleDelete = async (id) => {
  await paperReadingApi.delete(id)
  ElMessage.success('删除成功')
  loadData()
}

const handleUploadSuccess = (res) => {
  if (res.code === 200) {
    form.attachmentUrl = res.data.url
    ElMessage.success('上传成功')
  }
}

const downloadFile = (url) => {
  window.open(url, '_blank')
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (editId.value) {
      await paperReadingApi.update(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await paperReadingApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
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

.attachment-icon {
  color: #4080FF;
  cursor: pointer;
  font-size: 18px;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s ease;
  
  &:hover {
    color: #6AA1FF;
    background-color: #E8F3FF;
    transform: scale(1.1);
  }
}

.no-attachment {
  color: #C9CDD4;
}

.no-attachment-text {
  color: #86909C;
  font-size: 14px;
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

.upload-area {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.upload-tip {
  font-size: 12px;
  color: #86909C;
  margin-top: 8px;
}

.attachment-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #4080FF;
  text-decoration: none;
  font-size: 13px;
  padding: 6px 12px;
  background-color: #E8F3FF;
  border-radius: 6px;
  transition: all 0.2s ease;
  
  &:hover {
    background-color: #D6E8FF;
    transform: translateY(-1px);
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
