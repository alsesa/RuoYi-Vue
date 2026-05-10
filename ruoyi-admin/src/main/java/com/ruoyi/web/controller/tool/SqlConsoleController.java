package com.ruoyi.web.controller.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;

/**
 * SQL控制台
 */
@RestController
@RequestMapping("/tool/sqlConsole")
public class SqlConsoleController extends BaseController
{
    @Autowired
    private DataSource dataSource;

    /**
     * 获取SQL查询的列名
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/columns")
    public AjaxResult columns(@RequestBody Map<String, String> params)
    {
        String sql = params.get("sql");
        if (sql == null || sql.trim().isEmpty())
        {
            return AjaxResult.error("SQL语句不能为空");
        }
        sql = sql.trim();

        try
        {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            // 直接在原查询末尾追加 LIMIT 0，只获取列元数据不返回数据
            String columnSql = sql + " LIMIT 0";
            List<String> columns = jdbcTemplate.query(columnSql, rs -> {
                List<String> cols = new ArrayList<>();
                int count = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= count; i++)
                {
                    cols.add(rs.getMetaData().getColumnLabel(i));
                }
                return cols;
            });
            return AjaxResult.success(columns);
        }
        catch (Exception e)
        {
            return AjaxResult.error("获取列名失败: " + e.getMessage());
        }
    }

    /**
     * 执行SQL语句
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "SQL控制台", businessType = BusinessType.OTHER)
    @PostMapping("/execute")
    public AjaxResult execute(@RequestBody Map<String, String> params)
    {
        String sql = params.get("sql");
        if (sql == null || sql.trim().isEmpty())
        {
            return AjaxResult.error("SQL语句不能为空");
        }
        sql = sql.trim();

        try
        {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            String upperSql = sql.toUpperCase().replaceAll("^\\s+", "");

            if (upperSql.startsWith("SELECT") || upperSql.startsWith("SHOW") || upperSql.startsWith("DESCRIBE") || upperSql.startsWith("DESC") || upperSql.startsWith("EXPLAIN"))
            {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
                List<String> columns = new ArrayList<>();
                if (!rows.isEmpty())
                {
                    columns.addAll(rows.get(0).keySet());
                }
                Map<String, Object> data = new HashMap<>();
                data.put("type", "select");
                data.put("columns", columns);
                data.put("rows", rows);
                return AjaxResult.success(data);
            }
            else if (upperSql.startsWith("INSERT") || upperSql.startsWith("UPDATE") || upperSql.startsWith("DELETE"))
            {
                int affectedRows = jdbcTemplate.update(sql);
                Map<String, Object> data = new HashMap<>();
                data.put("type", "dml");
                data.put("affectedRows", affectedRows);
                return AjaxResult.success(data);
            }
            else
            {
                jdbcTemplate.execute(sql);
                Map<String, Object> data = new HashMap<>();
                data.put("type", "ddl");
                data.put("message", "执行成功");
                return AjaxResult.success(data);
            }
        }
        catch (Exception e)
        {
            return AjaxResult.error("执行失败: " + e.getMessage());
        }
    }

    /**
     * 解析SQL中的表名，获取所有表的列信息（table.column格式）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "SQL控制台", businessType = BusinessType.OTHER)
    @PostMapping("/tableColumns")
    public AjaxResult tableColumns(@RequestBody Map<String, String> params)
    {
        String sql = params.get("sql");
        if (sql == null || sql.trim().isEmpty())
        {
            return AjaxResult.error("SQL语句不能为空");
        }
        sql = sql.trim();

        try
        {
            Set<String> tableNames = extractTableNames(sql);
            if (tableNames.isEmpty())
            {
                return AjaxResult.error("未能从SQL中解析到表名");
            }

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            List<String> result = new ArrayList<>();
            for (String table : tableNames)
            {
                List<String> columns = jdbcTemplate.queryForList(
                    "SELECT column_name FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? ORDER BY ordinal_position",
                    String.class, table);
                for (String col : columns)
                {
                    result.add(table + "." + col);
                }
            }
            return AjaxResult.success(result);
        }
        catch (Exception e)
        {
            return AjaxResult.error("获取表字段失败: " + e.getMessage());
        }
    }

    /**
     * 从SQL语句中提取表名（支持FROM、JOIN子句）
     */
    private Set<String> extractTableNames(String sql)
    {
        Set<String> tables = new LinkedHashSet<>();
        // 去掉SQL中的字符串常量和注释，避免干扰解析
        String cleanSql = sql.replaceAll("'[^']*'", "")
                             .replaceAll("--[^\n]*", "")
                             .replaceAll("/\\*[\\s\\S]*?\\*/", "");
        // 匹配 FROM 和 JOIN 后面的表名（可选 schema 前缀，可选别名）
        Pattern pattern = Pattern.compile("(?:FROM|JOIN)\\s+(`?\\w+`?\\.?`?\\w+`?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(cleanSql);
        while (matcher.find())
        {
            String raw = matcher.group(1).replace("`", "");
            // 如果是 schema.table 格式，只取 table 部分
            if (raw.contains("."))
            {
                raw = raw.substring(raw.indexOf('.') + 1);
            }
            // 跳过子查询产生的空匹配
            if (!raw.isEmpty() && !raw.startsWith("("))
            {
                tables.add(raw);
            }
        }
        return tables;
    }
}
