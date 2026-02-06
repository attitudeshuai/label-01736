<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="layout-aside">
      <div class="logo">
        <el-icon :size="24" color="#4080FF"><Reading /></el-icon>
        <span v-if="!isCollapse" class="logo-text">科研管理</span>
      </div>
      
      <el-menu 
        :default-active="route.path" 
        :collapse="isCollapse" 
        router 
        background-color="#001529" 
        text-color="rgba(255,255,255,0.65)" 
        active-text-color="#fff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页概览</template>
        </el-menu-item>
        <el-menu-item index="/paper-reading">
          <el-icon><Reading /></el-icon>
          <template #title>论文阅读</template>
        </el-menu-item>
        <el-menu-item index="/achievement">
          <el-icon><Trophy /></el-icon>
          <template #title>成果登记</template>
        </el-menu-item>
        <el-menu-item index="/weekly-report">
          <el-icon><Document /></el-icon>
          <template #title>周报管理</template>
        </el-menu-item>
        <el-menu-item v-if="userStore.isAdmin" index="/user">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item v-if="userStore.isAdmin" index="/operation-log">
          <el-icon><List /></el-icon>
          <template #title>操作日志</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container class="main-container">
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <div class="user-role">
            <el-tag size="small" :type="roleTagType">{{ roleText }}</el-tag>
          </div>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" class="user-avatar">
                {{ userStore.userInfo.realName?.charAt(0) }}
              </el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ userStore.userInfo.realName }}</span>
                <span class="user-account">{{ userStore.userInfo.username }}</span>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
    
    <el-dialog v-model="passwordVisible" title="修改密码" width="450px" destroy-on-close>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px" class="password-form">
        <div class="form-section">
          <div class="section-title">
            <el-icon><Lock /></el-icon>
            <span>密码修改</span>
          </div>
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="passwordVisible = false">取消</el-button>
          <el-button type="primary" :loading="pwdLoading" @click="handleChangePassword">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)
const passwordVisible = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref()

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const roleText = computed(() => {
  const roles = { 1: '管理员', 2: '导师', 3: '学生' }
  return roles[userStore.userInfo.role] || ''
})

const roleTagType = computed(() => {
  const types = { 1: 'danger', 2: 'warning', 3: '' }
  return types[userStore.userInfo.role] || ''
})

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
  } else if (command === 'password') {
    passwordVisible.value = true
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  }
}

const handleChangePassword = async () => {
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await authApi.changePassword(pwdForm.oldPassword, pwdForm.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    passwordVisible.value = false
    userStore.logout()
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background: linear-gradient(180deg, #001529 0%, #001D3D 100%);
  transition: width 0.3s cubic-bezier(0.2, 0, 0, 1);
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  
  .logo {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    background: rgba(255, 255, 255, 0.03);
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    
    .logo-text {
      color: white;
      font-size: 18px;
      font-weight: 600;
      white-space: nowrap;
      letter-spacing: 1px;
    }
  }
  
  :deep(.el-menu) {
    border-right: none;
    padding: 12px 8px;
    background: transparent;
    
    .el-menu-item {
      height: 46px;
      line-height: 46px;
      margin-bottom: 6px;
      border-radius: 10px;
      transition: all 0.25s ease;
      
      &:hover {
        background-color: rgba(255, 255, 255, 0.08) !important;
        transform: translateX(4px);
      }
      
      &.is-active {
        background: linear-gradient(135deg, #4080FF 0%, #6AA1FF 100%) !important;
        box-shadow: 0 4px 12px rgba(64, 128, 255, 0.4);
        
        .el-icon {
          color: white;
        }
      }
      
      .el-icon {
        font-size: 18px;
        transition: transform 0.2s ease;
      }
      
      &:hover .el-icon {
        transform: scale(1.1);
      }
    }
  }
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #F2F3F5;
}

.layout-header {
  height: 64px;
  background: white;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 10;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: #4E5969;
      padding: 10px;
      border-radius: 8px;
      transition: all 0.25s ease;
      
      &:hover {
        color: #4080FF;
        background-color: #E8F3FF;
        transform: scale(1.05);
      }
    }
    
    :deep(.el-breadcrumb) {
      .el-breadcrumb__item {
        .el-breadcrumb__inner {
          font-weight: 500;
          
          &.is-link:hover {
            color: #4080FF;
          }
        }
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .user-role {
      :deep(.el-tag) {
        border-radius: 6px;
        font-weight: 500;
        padding: 0 10px;
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      padding: 8px 14px;
      border-radius: 10px;
      transition: all 0.25s ease;
      
      &:hover {
        background-color: #F2F3F5;
        
        .dropdown-icon {
          transform: rotate(180deg);
        }
      }
      
      .user-avatar {
        background: linear-gradient(135deg, #4080FF 0%, #6AA1FF 100%);
        color: white;
        font-weight: 600;
        box-shadow: 0 2px 8px rgba(64, 128, 255, 0.35);
      }
      
      .user-detail {
        display: flex;
        flex-direction: column;
        
        .user-name {
          font-size: 14px;
          font-weight: 600;
          color: #1D2129;
          line-height: 1.4;
        }
        
        .user-account {
          font-size: 12px;
          color: #86909C;
          line-height: 1.4;
        }
      }
      
      .dropdown-icon {
        color: #86909C;
        font-size: 12px;
        transition: transform 0.25s ease;
      }
    }
  }
}

.layout-main {
  background-color: #F2F3F5;
  padding: 24px;
  overflow-y: auto;
}

// 下拉菜单样式
:deep(.el-dropdown-menu) {
  padding: 6px;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  
  .el-dropdown-menu__item {
    border-radius: 6px;
    padding: 10px 16px;
    font-size: 14px;
    
    &:hover {
      background-color: #E8F3FF;
      color: #4080FF;
    }
    
    .el-icon {
      margin-right: 8px;
    }
  }
}

// 页面切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

// 密码修改弹框样式
.password-form {
  .form-section {
    padding: 20px;
    background: #F7F8FA;
    border-radius: 12px;
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
