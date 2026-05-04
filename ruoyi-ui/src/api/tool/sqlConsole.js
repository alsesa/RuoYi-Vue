import request from '@/utils/request'

// 执行SQL语句
export function executeSql(sql) {
  return request({
    url: '/tool/sqlConsole/execute',
    method: 'post',
    data: { sql }
  })
}

// 获取SQL查询的列名
export function getSqlColumns(sql) {
  return request({
    url: '/tool/sqlConsole/columns',
    method: 'post',
    data: { sql }
  })
}
