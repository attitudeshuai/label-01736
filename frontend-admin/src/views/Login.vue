<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="login-bg-shape shape-1"></div>
      <div class="login-bg-shape shape-2"></div>
      <div class="login-bg-shape shape-3"></div>
      <div class="login-bg-shape shape-4"></div>
    </div>
    
    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">
          <el-icon :size="32" color="#4080FF"><Reading /></el-icon>
        </div>
        <h1>研究生培养科研管理系统</h1>
        <p>Graduate Research Management System</p>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名" 
            prefix-icon="User" 
            size="large"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock" 
            size="large" 
            show-password 
            @keyup.enter="handleLogin" 
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading" 
            class="login-btn" 
            @click="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  
  .login-bg-shape {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.08);
    animation: float 20s infinite ease-in-out;
    backdrop-filter: blur(1px);
  }
  
  .shape-1 {
    width: 500px;
    height: 500px;
    top: -150px;
    left: -150px;
    animation-delay: 0s;
  }
  
  .shape-2 {
    width: 350px;
    height: 350px;
    bottom: -80px;
    right: -80px;
    animation-delay: -5s;
  }
  
  .shape-3 {
    width: 250px;
    height: 250px;
    top: 40%;
    right: 15%;
    animation-delay: -10s;
  }
  
  .shape-4 {
    width: 150px;
    height: 150px;
    bottom: 30%;
    left: 10%;
    animation-delay: -15s;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg) scale(1);
  }
  25% {
    transform: translateY(-15px) rotate(5deg) scale(1.02);
  }
  50% {
    transform: translateY(-25px) rotate(10deg) scale(1.05);
  }
  75% {
    transform: translateY(-10px) rotate(5deg) scale(1.02);
  }
}

.login-card {
  width: 440px;
  padding: 52px 44px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 25px 60px -12px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
  animation: cardAppear 0.6s ease;
}

@keyframes cardAppear {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 44px;
  
  .login-logo {
    width: 72px;
    height: 72px;
    margin: 0 auto 20px;
    background: linear-gradient(135deg, #E8F3FF 0%, #D6E8FF 100%);
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8px 20px rgba(64, 128, 255, 0.2);
    animation: logoFloat 3s ease-in-out infinite;
  }
  
  @keyframes logoFloat {
    0%, 100% {
      transform: translateY(0);
    }
    50% {
      transform: translateY(-6px);
    }
  }
  
  h1 {
    font-size: 24px;
    color: #1D2129;
    margin: 0 0 10px;
    font-weight: 700;
    letter-spacing: 0.5px;
  }
  
  p {
    font-size: 13px;
    color: #86909C;
    margin: 0;
    letter-spacing: 0.5px;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 26px;
  }
  
  :deep(.el-input__wrapper) {
    padding: 6px 14px;
    border-radius: 10px;
    box-shadow: 0 0 0 1px #E5E6EB inset;
    transition: all 0.25s ease;
    
    &:hover {
      box-shadow: 0 0 0 1px #4080FF inset;
    }
    
    &.is-focus {
      box-shadow: 0 0 0 1px #4080FF inset, 0 0 0 4px rgba(64, 128, 255, 0.12);
    }
  }
  
  :deep(.el-input__inner) {
    height: 44px;
    font-size: 15px;
  }
  
  :deep(.el-input__prefix) {
    font-size: 18px;
    color: #86909C;
  }
}

.login-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #4080FF 0%, #6AA1FF 100%);
  border: none;
  box-shadow: 0 6px 16px rgba(64, 128, 255, 0.45);
  transition: all 0.3s ease;
  letter-spacing: 2px;
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 10px 24px rgba(64, 128, 255, 0.55);
  }
  
  &:active {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(64, 128, 255, 0.45);
  }
}
</style>
