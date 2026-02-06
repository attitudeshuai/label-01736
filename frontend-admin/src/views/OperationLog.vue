<template>
  <div class="operation-log">
    <div class="page-card">
      <div class="page-header">
        <span class="page-title">操作日志</span>
      </div>
      
      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="搜索操作/用户名" clearable style="width: 250px" @keyup.enter="loadData" />
        <el-button type="primary" icon="Search" @click="loadData">搜索</el-button>
      </div>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="username" label="操作用户" width="120" />
        <el-table-column prop="operation" label="操作" width="150" />
        <el-table-column prop="method" label="请求方法" min-width="250" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时" width="100">
          <template #default="{ row }">{{ row.costTime }}ms</template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" />
      </el-table>
      
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" background class="pagination" @change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { operationLogApi } from '@/api/operationLog'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({ page: 1, size: 10, keyword: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await operationLogApi.list(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

loadData()
</script>

<style lang="scss" scoped>
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #F2F3F5;
}

:deep(.el-table) {
  .el-table__cell {
    font-size: 13px;
  }
}
</style>
