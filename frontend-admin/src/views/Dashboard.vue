<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>{{ greeting }}，{{ userStore.userInfo.realName }} 👋</h1>
          <p>{{ currentDate }}，祝您工作顺利！</p>
        </div>
        <div class="welcome-stats">
          <div class="quick-stat">
            <span class="quick-stat-value">{{ stats.paperCount || 0 }}</span>
            <span class="quick-stat-label">论文阅读</span>
          </div>
          <div class="quick-stat">
            <span class="quick-stat-value">{{ stats.achievementCount || 0 }}</span>
            <span class="quick-stat-label">成果登记</span>
          </div>
          <div class="quick-stat">
            <span class="quick-stat-value">{{ stats.reportCount || 0 }}</span>
            <span class="quick-stat-label">周报数量</span>
          </div>
        </div>
      </div>
      <div class="welcome-illustration">
        <el-icon :size="120" color="rgba(255,255,255,0.2)"><Reading /></el-icon>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card--primary">
          <div class="stat-content">
            <div class="stat-value">{{ stats.paperCount || 0 }}</div>
            <div class="stat-label">论文阅读</div>
          </div>
          <el-icon class="stat-icon"><Reading /></el-icon>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card--success">
          <div class="stat-content">
            <div class="stat-value">{{ stats.achievementCount || 0 }}</div>
            <div class="stat-label">成果登记</div>
          </div>
          <el-icon class="stat-icon"><Trophy /></el-icon>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card--warning">
          <div class="stat-content">
            <div class="stat-value">{{ stats.reportCount || 0 }}</div>
            <div class="stat-label">周报数量</div>
          </div>
          <el-icon class="stat-icon"><Document /></el-icon>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card--danger">
          <div class="stat-content">
            <div class="stat-value">{{ stats.pendingCount || 0 }}</div>
            <div class="stat-label">待审阅</div>
          </div>
          <el-icon class="stat-icon"><Bell /></el-icon>
        </div>
      </el-col>
    </el-row>
    
    <!-- 数据列表 -->
    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <div class="page-card data-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon><Reading /></el-icon>
              <span>最近论文阅读</span>
            </div>
            <el-button link type="primary" @click="$router.push('/paper-reading')">
              查看更多 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <el-table :data="recentPapers" stripe style="width: 100%">
            <el-table-column prop="title" label="论文题目" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="paper-title">{{ row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="readingDate" label="阅读日期" width="110" align="center">
              <template #default="{ row }">
                <span class="date-text">{{ row.readingDate }}</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="recentPapers.length === 0" description="暂无论文阅读记录" :image-size="80" />
        </div>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <div class="page-card data-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon><Document /></el-icon>
              <span>最近周报</span>
            </div>
            <el-button link type="primary" @click="$router.push('/weekly-report')">
              查看更多 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <el-table :data="recentReports" stripe style="width: 100%">
            <el-table-column label="周报" min-width="140">
              <template #default="{ row }">
                <span class="report-title">{{ row.year }}年第{{ row.weekNumber }}周</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small" effect="light">
                  {{ statusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="recentReports.length === 0" description="暂无周报记录" :image-size="80" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { paperReadingApi } from '@/api/paperReading'
import { weeklyReportApi } from '@/api/weeklyReport'
import { achievementApi } from '@/api/achievement'
import dayjs from 'dayjs'

const userStore = useUserStore()

const stats = ref({})
const recentPapers = ref([])
const recentReports = ref([])

const currentDate = computed(() => dayjs().format('YYYY年MM月DD日 dddd'))

const greeting = computed(() => {
  const hour = dayjs().hour()
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const statusType = (status) => {
  const types = { 1: 'info', 2: 'warning', 3: 'success' }
  return types[status]
}

const statusText = (status) => {
  const texts = { 1: '草稿', 2: '已提交', 3: '已审阅' }
  return texts[status]
}

const loadData = async () => {
  try {
    const [paperRes, reportRes, achievementRes] = await Promise.all([
      paperReadingApi.list({ page: 1, size: 5 }),
      weeklyReportApi.list({ page: 1, size: 5 }),
      achievementApi.getStatistics()
    ])
    
    recentPapers.value = paperRes.data.records
    recentReports.value = reportRes.data.records
    
    stats.value = {
      paperCount: paperRes.data.total,
      reportCount: reportRes.data.total,
      achievementCount: achievementRes.data.total,
      pendingCount: recentReports.value.filter(r => r.status === 2).length
    }
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.dashboard {
  .welcome-section {
    background: linear-gradient(135deg, #4080FF 0%, #6AA1FF 100%);
    border-radius: 16px;
    padding: 36px 40px;
    margin-bottom: 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    overflow: hidden;
    box-shadow: 0 8px 24px rgba(64, 128, 255, 0.35);
    
    &::before {
      content: '';
      position: absolute;
      top: -50%;
      right: -20%;
      width: 80%;
      height: 150%;
      background: radial-gradient(circle, rgba(255, 255, 255, 0.12) 0%, transparent 70%);
      border-radius: 50%;
    }
    
    .welcome-content {
      position: relative;
      z-index: 1;
    }
    
    .welcome-text {
      h1 {
        font-size: 26px;
        font-weight: 600;
        color: white;
        margin: 0 0 10px;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
      
      p {
        font-size: 14px;
        color: rgba(255, 255, 255, 0.9);
        margin: 0;
      }
    }
    
    .welcome-stats {
      display: flex;
      gap: 40px;
      margin-top: 28px;
      
      .quick-stat {
        display: flex;
        flex-direction: column;
        position: relative;
        
        &:not(:last-child)::after {
          content: '';
          position: absolute;
          right: -20px;
          top: 50%;
          transform: translateY(-50%);
          width: 1px;
          height: 36px;
          background: rgba(255, 255, 255, 0.2);
        }
        
        .quick-stat-value {
          font-size: 32px;
          font-weight: 700;
          color: white;
          line-height: 1.2;
          text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }
        
        .quick-stat-label {
          font-size: 13px;
          color: rgba(255, 255, 255, 0.8);
          margin-top: 6px;
        }
      }
    }
    
    .welcome-illustration {
      position: absolute;
      right: 50px;
      top: 50%;
      transform: translateY(-50%);
      opacity: 0.15;
    }
  }
  
  .stat-row {
    margin-bottom: 24px;
  }
  
  .stat-card {
    border-radius: 14px;
    padding: 24px;
    color: white;
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    &:hover {
      transform: translateY(-6px) scale(1.02);
      
      .stat-icon {
        transform: scale(1.15) rotate(5deg);
        opacity: 0.35;
      }
    }
    
    &::before {
      content: '';
      position: absolute;
      top: -40%;
      right: -30%;
      width: 100%;
      height: 140%;
      background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
      border-radius: 50%;
    }
    
    .stat-content {
      position: relative;
      z-index: 1;
    }
    
    .stat-value {
      font-size: 34px;
      font-weight: 700;
      line-height: 1.2;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
    
    .stat-label {
      font-size: 14px;
      opacity: 0.9;
      margin-top: 6px;
      font-weight: 500;
    }
    
    .stat-icon {
      font-size: 60px;
      opacity: 0.25;
      transition: all 0.3s ease;
    }
    
    &--primary {
      background: linear-gradient(135deg, #4080FF 0%, #6AA1FF 100%);
      box-shadow: 0 8px 20px rgba(64, 128, 255, 0.35);
      
      &:hover {
        box-shadow: 0 12px 28px rgba(64, 128, 255, 0.45);
      }
    }
    
    &--success {
      background: linear-gradient(135deg, #00B42A 0%, #23C343 100%);
      box-shadow: 0 8px 20px rgba(0, 180, 42, 0.35);
      
      &:hover {
        box-shadow: 0 12px 28px rgba(0, 180, 42, 0.45);
      }
    }
    
    &--warning {
      background: linear-gradient(135deg, #FF7D00 0%, #FF9A2E 100%);
      box-shadow: 0 8px 20px rgba(255, 125, 0, 0.35);
      
      &:hover {
        box-shadow: 0 12px 28px rgba(255, 125, 0, 0.45);
      }
    }
    
    &--danger {
      background: linear-gradient(135deg, #F53F3F 0%, #F76560 100%);
      box-shadow: 0 8px 20px rgba(245, 63, 63, 0.35);
      
      &:hover {
        box-shadow: 0 12px 28px rgba(245, 63, 63, 0.45);
      }
    }
  }
  
  .data-card {
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid #F2F3F5;
      
      .card-title {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 16px;
        font-weight: 600;
        color: #1D2129;
        
        .el-icon {
          color: #4080FF;
          font-size: 20px;
        }
      }
      
      .el-button {
        font-size: 13px;
        font-weight: 500;
        
        .el-icon {
          margin-left: 4px;
          transition: transform 0.2s ease;
        }
        
        &:hover .el-icon {
          transform: translateX(3px);
        }
      }
    }
    
    .paper-title, .report-title {
      font-size: 14px;
      color: #1D2129;
      font-weight: 500;
    }
    
    .date-text {
      font-size: 13px;
      color: #86909C;
    }
    
    :deep(.el-table) {
      --el-table-border-color: transparent;
      
      th.el-table__cell {
        background-color: #F7F8FA !important;
        font-size: 13px;
        padding: 12px;
      }
      
      td.el-table__cell {
        padding: 14px 12px;
      }
      
      .el-table__row:hover > td.el-table__cell {
        background-color: #E8F3FF !important;
      }
    }
    
    :deep(.el-empty) {
      padding: 40px 0;
      
      .el-empty__description {
        margin-top: 12px;
        color: #86909C;
      }
    }
  }
}

@media (max-width: 768px) {
  .dashboard {
    .welcome-section {
      padding: 24px;
      border-radius: 12px;
      
      .welcome-text h1 {
        font-size: 20px;
      }
      
      .welcome-stats {
        gap: 24px;
        
        .quick-stat {
          &:not(:last-child)::after {
            right: -12px;
          }
        }
        
        .quick-stat-value {
          font-size: 24px;
        }
      }
      
      .welcome-illustration {
        display: none;
      }
    }
    
    .stat-card {
      padding: 18px;
      margin-bottom: 12px;
      border-radius: 10px;
      
      .stat-value {
        font-size: 26px;
      }
      
      .stat-icon {
        font-size: 44px;
      }
    }
    
    .data-card {
      .card-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
      }
    }
  }
}
</style>
