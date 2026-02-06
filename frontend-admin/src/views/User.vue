<template>
  <div class="user-management">
    <div class="page-card">
      <div class="page-header">
        <span class="page-title">用户管理</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增用户
        </el-button>
      </div>
      
      <div class="search-bar">
        <el-select v-model="query.role" placeholder="角色" clearable style="width: 120px">
          <el-option label="管理员" :value="1" />
          <el-option label="导师" :value="2" />
          <el-option label="学生" :value="3" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="搜索用户名/姓名/学号" clearable style="width: 250px" @keyup.enter="loadData">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadData">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
      
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="username" label="用户名" min-width="100" />
        <el-table-column prop="realName" label="姓名" min-width="90" />
        <el-table-column prop="role" label="角色" min-width="90">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)" size="small">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="studentId" label="学号" min-width="110" />
        <el-table-column prop="supervisorName" label="导师" min-width="90" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-popconfirm v-if="row.role !== 1" title="确定删除该用户吗？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button link type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" background @change="loadData" />
      </div>
    </div>
    
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑用户' : '新增用户'" width="650px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="dialog-form">
        <!-- 账号信息区域 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><User /></el-icon>
            <span>账号信息</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="form.username" placeholder="请输入用户名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="密码" :prop="editId ? '' : 'password'">
                <el-input v-model="form.password" type="password" show-password :placeholder="editId ? '留空则不修改' : '请输入密码'" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="姓名" prop="realName">
                <el-input v-model="form.realName" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="角色" prop="role">
                <el-select v-model="form.role" style="width: 100%">
                  <el-option label="管理员" :value="1" />
                  <el-option label="导师" :value="2" />
                  <el-option label="学生" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        
        <!-- 学生信息区域 -->
        <div class="form-section" v-if="form.role === 3">
          <div class="section-title">
            <el-icon><School /></el-icon>
            <span>学生信息</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="学号">
                <el-input v-model="form.studentId" placeholder="请输入学号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="导师">
                <el-select v-model="form.supervisorId" style="width: 100%" clearable placeholder="请选择导师">
                  <el-option v-for="t in teachers" :key="t.id" :label="t.realName" :value="t.id" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        
        <!-- 联系方式区域 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Phone /></el-icon>
            <span>联系方式</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="邮箱">
                <el-input v-model="form.email" placeholder="请输入邮箱" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号">
                <el-input v-model="form.phone" placeholder="请输入手机号" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        
        <!-- 其他信息区域 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>其他信息</span>
          </div>
          <el-form-item label="研究方向">
            <el-input v-model="form.researchDirection" placeholder="请输入研究方向" />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { userApi } from '@/api/user'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const editId = ref(null)
const tableData = ref([])
const teachers = ref([])
const total = ref(0)
const formRef = ref()

const query = reactive({ page: 1, size: 10, role: null, keyword: '' })

const form = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  role: 3,
  supervisorId: null,
  studentId: '',
  researchDirection: '',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const roleText = (role) => ({ 1: '管理员', 2: '导师', 3: '学生' }[role])
const roleTagType = (role) => ({ 1: 'danger', 2: 'warning', 3: 'primary' }[role])

const loadData = async () => {
  loading.value = true
  try {
    const res = await userApi.list(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.role = null
  query.keyword = ''
  query.page = 1
  loadData()
}

const loadTeachers = async () => {
  const res = await userApi.getTeachers()
  teachers.value = res.data
}

const resetForm = () => {
  form.username = ''
  form.password = ''
  form.realName = ''
  form.email = ''
  form.phone = ''
  form.role = 3
  form.supervisorId = null
  form.studentId = ''
  form.researchDirection = ''
  form.status = 1
  editId.value = null
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  editId.value = row.id
  const res = await userApi.getById(row.id)
  Object.assign(form, res.data)
  form.password = ''
  dialogVisible.value = true
}

const handleDelete = async (id) => {
  await userApi.delete(id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (editId.value) {
      await userApi.update(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await userApi.create(form)
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
  loadTeachers()
})
</script>

<style lang="scss" scoped>
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

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #F2F3F5;
}
</style>
