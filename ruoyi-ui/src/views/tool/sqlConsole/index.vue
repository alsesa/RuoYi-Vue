<template>
  <div class="app-container">
    <el-alert
      title="仅管理员可使用此功能，请谨慎操作，SQL语句将直接在数据库中执行。"
      type="warning"
      show-icon
      :closable="false"
      style="margin-bottom: 15px;"
    />

    <el-input
      v-model="sql"
      type="textarea"
      :rows="8"
      placeholder="请输入SQL语句，例如：select * from sys_user limit 10"
    />

    <div style="margin: 10px 0;">
      <el-button type="primary" :loading="loading" @click="handleExecute">执 行</el-button>
      <el-button :loading="loadingColumns" @click="handleShowColumns">查看字段</el-button>
      <el-button :loading="loadingTableColumns" @click="handleShowTableColumns">查看表字段</el-button>
      <el-button @click="handleClear">清 空</el-button>
    </div>

    <el-alert
      v-if="errorMsg"
      :title="errorMsg"
      type="error"
      show-icon
      style="margin-bottom: 15px;"
    />

    <el-alert
      v-if="successMsg"
      :title="successMsg"
      type="success"
      show-icon
      style="margin-bottom: 15px;"
    />

    <div v-if="columnList.length > 0" style="margin-bottom: 15px;">
      <el-tag v-for="col in columnList" :key="col" style="margin-right: 8px; margin-bottom: 5px;">{{ col }}</el-tag>
    </div>

    <div v-if="tableColumnList.length > 0" style="margin-bottom: 15px;">
      <el-tag v-for="col in tableColumnList" :key="col" type="success" style="margin-right: 8px; margin-bottom: 5px;">{{ col }}</el-tag>
    </div>

    <el-table
      v-if="columns.length > 0"
      :data="rows"
      border
      style="width: 100%;"
      max-height="500"
    >
      <el-table-column
        v-for="col in columns"
        :key="col"
        :prop="col"
        :label="col"
        :show-overflow-tooltip="true"
      />
    </el-table>
  </div>
</template>

<script>
import { executeSql, getSqlColumns, getTableColumns } from '@/api/tool/sqlConsole'

export default {
  name: 'SqlConsole',
  data() {
    return {
      sql: '',
      loading: false,
      loadingColumns: false,
      loadingTableColumns: false,
      columns: [],
      rows: [],
      columnList: [],
      tableColumnList: [],
      errorMsg: '',
      successMsg: ''
    }
  },
  methods: {
    handleExecute() {
      if (!this.sql.trim()) {
        this.errorMsg = '请输入SQL语句'
        return
      }
      this.loading = true
      this.errorMsg = ''
      this.successMsg = ''
      this.columns = []
      this.rows = []
      executeSql(this.sql).then(res => {
        const data = res.data
        if (data.type === 'select') {
          this.columns = data.columns
          this.rows = data.rows
          if (data.rows.length === 0) {
            this.successMsg = '查询成功，无数据'
          } else {
            this.successMsg = '查询成功，共 ' + data.rows.length + ' 条数据'
          }
        } else if (data.type === 'dml') {
          this.successMsg = '执行成功，影响 ' + data.affectedRows + ' 行'
        } else {
          this.successMsg = data.message || '执行成功'
        }
      }).catch(err => {
        this.errorMsg = err.msg || err.message || '执行失败'
      }).finally(() => {
        this.loading = false
      })
    },
    handleClear() {
      this.sql = ''
      this.columns = []
      this.rows = []
      this.columnList = []
      this.tableColumnList = []
      this.errorMsg = ''
      this.successMsg = ''
    },
    handleShowColumns() {
      if (!this.sql.trim()) {
        this.errorMsg = '请输入SQL语句'
        return
      }
      this.loadingColumns = true
      this.errorMsg = ''
      this.columnList = []
      getSqlColumns(this.sql).then(res => {
        this.columnList = res.data
        if (res.data.length === 0) {
          this.successMsg = '未获取到字段信息'
        } else {
          this.successMsg = '共 ' + res.data.length + ' 个字段'
        }
      }).catch(err => {
        this.errorMsg = err.msg || err.message || '获取字段失败'
      }).finally(() => {
        this.loadingColumns = false
      })
    },
    handleShowTableColumns() {
      if (!this.sql.trim()) {
        this.errorMsg = '请输入SQL语句'
        return
      }
      this.loadingTableColumns = true
      this.errorMsg = ''
      this.tableColumnList = []
      getTableColumns(this.sql).then(res => {
        this.tableColumnList = res.data
        if (res.data.length === 0) {
          this.successMsg = '未获取到表字段信息'
        } else {
          this.successMsg = '共 ' + res.data.length + ' 个表字段'
        }
      }).catch(err => {
        this.errorMsg = err.msg || err.message || '获取表字段失败'
      }).finally(() => {
        this.loadingTableColumns = false
      })
    }
  }
}
</script>
