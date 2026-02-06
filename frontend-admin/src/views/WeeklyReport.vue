<template>
  <div class="weekly-report">
    <div class="page-card">
      <div class="page-header">
        <span class="page-title">周报管理</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增周报
        </el-button>
      </div>
      
      <div class="search-bar">
        <el-select v-model="query.year" placeholder="年份" clearable style="width: 120px">
          <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="草稿" :value="1" />
          <el-option label="已提交" :value="2" />
          <el-option label="已审阅" :value="3" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
      
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="周报" min-width="130">
          <template #default="{ row }">
            <span class="report-title">{{ row.year }}年第{{ row.weekNumber }}周</span>
          </template>
        </el-table-column>
        <el-table-column label="周期" min-width="180">
          <template #default="{ row }">
            <span class="date-range">{{ row.weekStart }} ~ {{ row.weekEnd }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="提交人" min-width="90" align="center" />
        <el-table-column prop="status" label="状态" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small" effect="light">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" min-width="160" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.updateTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="handleView(row)">查看</el-button>
              <el-button v-if="row.status === 1" link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button v-if="row.status === 1 && canSubmit(row)" link type="primary" @click="handleSubmitReport(row)">提交</el-button>
              <el-button v-if="row.status === 2 && canReview" link type="warning" @click="handleReview(row)">审阅</el-button>
              <el-popconfirm v-if="row.status === 1" title="确定删除该周报吗？" @confirm="handleDelete(row.id)">
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
        <!-- 周报时间区域 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Calendar /></el-icon>
            <span>周报时间</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="年份" prop="year">
                <el-select v-model="form.year" style="width: 100%">
                  <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="周数" prop="weekNumber">
                <el-input-number v-model="form.weekNumber" :min="1" :max="53" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="周开始日期" prop="weekStart">
                <el-date-picker v-model="form.weekStart" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="周结束日期" prop="weekEnd">
                <el-date-picker v-model="form.weekEnd" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        
        <!-- 工作内容区域 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            <span>工作内容</span>
          </div>
          <el-form-item label="本周工作内容">
            <el-input v-model="form.workContent" type="textarea" :rows="5" placeholder="请按类别填写本周开展的工作内容" />
          </el-form-item>
          <el-form-item label="当前进展情况">
            <el-input v-model="form.currentProgress" type="textarea" :rows="3" placeholder="描述当前的研究进展" />
          </el-form-item>
        </div>
        
        <!-- 下周计划区域 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Flag /></el-icon>
            <span>下周计划</span>
          </div>
          <el-form-item label="下周计划安排">
            <el-input v-model="form.nextWeekPlan" type="textarea" :rows="3" placeholder="下周的工作计划" />
          </el-form-item>
        </div>
        
        <!-- 导师评语区域 -->
        <div class="form-section" v-if="isView && form.supervisorComment">
          <div class="section-title">
            <el-icon><ChatDotRound /></el-icon>
            <span>导师评语</span>
          </div>
          <div class="supervisor-comment">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ form.supervisorComment }}</span>
          </div>
        </div>
      </el-form>
      <template #footer v-if="!isView">
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSave">保存</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 审阅对话框 -->
    <el-dialog v-model="reviewVisible" title="审阅周报" width="500px">
      <el-form label-width="80px" class="dialog-form">
        <div class="form-section">
          <div class="section-title">
            <el-icon><EditPen /></el-icon>
            <span>审阅评语</span>
          </div>
          <el-form-item label="评语">
            <el-input v-model="reviewComment" type="textarea" :rows="4" placeholder="请输入评语（可选）" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="reviewVisible = false">取消</el-button>
          <el-button type="primary" :loading="reviewLoading" @click="confirmReview">确认审阅</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { weeklyReportApi } from '@/api/weeklyReport'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import weekOfYear from 'dayjs/plugin/weekOfYear'
dayjs.extend(weekOfYear)

const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const reviewLoading = ref(false)
const dialogVisible = ref(false)
const reviewVisible = ref(false)
const isView = ref(false)
const editId = ref(null)
const reviewId = ref(null)
const reviewComment = ref('')
const tableData = ref([])
const total = ref(0)
const formRef = ref()

const currentYear = dayjs().year()
const years = Array.from({ length: 5 }, (_, i) => currentYear - i)

const query = reactive({ page: 1, size: 10, year: currentYear, status: null })

const form = reactive({
  year: currentYear,
  weekNumber: dayjs().week(),
  weekStart: '',
  weekEnd: '',
  workContent: '',
  currentProgress: '',
  nextWeekPlan: '',
  supervisorComment: ''
})

const rules = {
  year: [{ required: true, message: '请选择年份', trigger: 'change' }],
  weekNumber: [{ required: true, message: '请输入周数', trigger: 'blur' }],
  weekStart: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  weekEnd: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

const dialogTitle = computed(() => isView.value ? '查看周报' : (editId.value ? '编辑周报' : '新增周报'))
const canReview = computed(() => userStore.isAdmin || userStore.isTeacher)

const canSubmit = (row) => row.userId === userStore.userInfo.id

const statusType = (status) => ({ 1: 'info', 2: 'warning', 3: 'success' }[status])
const statusText = (status) => ({ 1: '草稿', 2: '已提交', 3: '已审阅' }[status])

const formatTime = (time) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await weeklyReportApi.list(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.year = currentYear
  query.status = null
  query.page = 1
  loadData()
}

const resetForm = () => {
  const now = dayjs()
  form.year = now.year()
  form.weekNumber = now.week()
  form.weekStart = now.startOf('week').add(1, 'day').format('YYYY-MM-DD')
  form.weekEnd = now.endOf('week').add(1, 'day').format('YYYY-MM-DD')
  form.workContent = ''
  form.currentProgress = ''
  form.nextWeekPlan = ''
  form.supervisorComment = ''
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
  const res = await weeklyReportApi.getById(row.id)
  Object.assign(form, res.data)
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  editId.value = row.id
  const res = await weeklyReportApi.getById(row.id)
  Object.assign(form, res.data)
  dialogVisible.value = true
}

const handleDelete = async (id) => {
  await weeklyReportApi.delete(id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmitReport = async (row) => {
  await weeklyReportApi.submit(row.id)
  ElMessage.success('提交成功')
  loadData()
}

const handleReview = (row) => {
  reviewId.value = row.id
  reviewComment.value = ''
  reviewVisible.value = true
}

const confirmReview = async () => {
  reviewLoading.value = true
  try {
    await weeklyReportApi.review(reviewId.value, reviewComment.value)
    ElMessage.success('审阅成功')
    reviewVisible.value = false
    loadData()
  } finally {
    reviewLoading.value = false
  }
}

const handleSave = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (editId.value) {
      await weeklyReportApi.update(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await weeklyReportApi.create(form)
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
.report-title {
  font-weight: 600;
  color: #1D2129;
}

.date-range {
  font-size: 13px;
  color: #4E5969;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  
  &::before {
    content: '';
    width: 4px;
    height: 4px;
    background-color: #C9CDD4;
    border-radius: 50%;
  }
}

.time-text {
  font-size: 13px;
  color: #86909C;
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

.supervisor-comment {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #E8F3FF 0%, #D6E8FF 100%);
  border-radius: 10px;
  color: #4E5969;
  line-height: 1.7;
  border-left: 4px solid #4080FF;
  
  .el-icon {
    color: #4080FF;
    margin-top: 3px;
    font-size: 18px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
