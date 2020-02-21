package com.smartnews.jpa_entity_generator.metadata;

import com.smartnews.jpa_entity_generator.config.JDBCSettings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fetches metadata for all tables in a given database.
 */
@Slf4j
public class TableMetadataFetcher {

    private static final String[] TABLE_TYPES = new String[]{"TABLE", "VIEW"};

    private DatabaseMetaData getMetadata(JDBCSettings settings) throws SQLException {
        try {
            Class.forName(settings.getDriverClassName());
        } catch (ClassNotFoundException e) {
            log.error("Failed to load JDBC driver (driver: {}, error: {})", settings.getDriverClassName(), e.getMessage(), e);
            throw new SQLException(e);
        }
        Connection connection = DriverManager.getConnection(settings.getUrl(), settings.getUsername(), settings.getPassword());
        return connection.getMetaData();
    }

    public List<String> getTableNames(JDBCSettings jdbcSettings) throws SQLException {
        DatabaseMetaData databaseMeta = getMetadata(jdbcSettings);
        try {
            List<String> tableNames = new ArrayList<>();
            try (ResultSet rs = databaseMeta.getTables(null,  jdbcSettings.getSchemaPattern(), "%", TABLE_TYPES)) {
                while (rs.next()) {
                    tableNames.add(rs.getString("TABLE_NAME"));
                }
            }
            return tableNames;
        } finally {
            databaseMeta.getConnection().close();
        }
    }

    public Table getTable(JDBCSettings jdbcSettings, String schemaAndTable) throws SQLException {

        Table tableInfo = new Table();

        String schema = extractSchema(schemaAndTable);
        String table = extractTabeName(schemaAndTable);
        tableInfo.setName(table);
        tableInfo.setSchema(Optional.ofNullable(schema));
        DatabaseMetaData databaseMeta = getMetadata(jdbcSettings);
        try {
            try (ResultSet rs = databaseMeta.getTables(null, schema, table, TABLE_TYPES)) {
                if (rs.next()) {
                    tableInfo.setDescription(Optional.ofNullable(rs.getString("REMARKS")));
                }
            } catch (Exception e) {
                log.debug("Failed to fetch table comment", e);
            }

            final List<String> primaryKeyNames = new ArrayList<>();
            try (ResultSet rs = databaseMeta.getPrimaryKeys(null, schema, table)) {
                while (rs.next()) {
                    primaryKeyNames.add(rs.getString("COLUMN_NAME"));
                }
            }
            try (ResultSet rs = databaseMeta.getColumns(null, schema, table, "%")) {
                while (rs.next()) {
                    Column column = new Column();
                    column.setName(rs.getString("COLUMN_NAME"));
                    //add by zhengyi，记录数据库字段信息
                    column.setLength(rs.getInt("COLUMN_SIZE"));
                    column.setTypeCode(rs.getInt("DATA_TYPE"));
                    column.setTypeName(rs.getString("TYPE_NAME"));

                    if(column.getTypeCode() == Types.DECIMAL) {
                        //add by zhengyi，对于DECIMAL类型字段，获取decimal精度与小数位数
                        column.setPrecision(column.getLength());
                        column.setDigits(rs.getInt("DECIMAL_DIGITS"));
                    }

                    // Oracle throws java.sql.SQLException: Invalid column name
                    boolean autoIncrement = false;
                    try {
                        String autoIncrementMetadata = rs.getString("IS_AUTOINCREMENT");
                        autoIncrement = autoIncrementMetadata != null && autoIncrementMetadata.equals("YES");
                    } catch (Exception e) {
                        log.debug("Failed to fetch auto_increment flag for {}.{}", table, column.getName(), e);
                    }
                    column.setAutoIncrement(autoIncrement);

                    try {
                        column.setDescription(Optional.ofNullable(rs.getString("REMARKS")));
                    } catch (Exception e) {
                        log.debug("Failed to fetch comment flag for {}.{}", table, column.getName(), e);
                    }

                    boolean nullable = true;
                    try {
                        String isNullableMetadata = rs.getString("IS_NULLABLE");
                        nullable = isNullableMetadata == null || isNullableMetadata.equals("YES");
                    } catch (Exception e) {
                        log.debug("Failed to fetch nullable flag for {}.{}", table, column.getName(), e);
                    }
                    column.setNullable(nullable);

                    boolean primaryKey = false;
                    try {
                        primaryKey = primaryKeyNames.stream().filter(pk -> pk.equals(columnName(rs))).count() > 0;
                    } catch (Exception e) {
                        log.debug("Failed to fetch primary key or not for {}.{}", table, column.getName(), e);
                    }
                    column.setPrimaryKey(primaryKey);

                    //add by zhengyi，打印字段信息
                    if(log.isDebugEnabled()) {
                        log.debug("column info: {}", column);
                    }

                    tableInfo.getColumns().add(column);
                }
            }

            //add by zhengyi，增加对索引信息的处理
            ResultSet indexRS = databaseMeta.getIndexInfo(null, schema, table, false, true);
            handelIndexInfo(indexRS, tableInfo.getIndexInfoList());

            return tableInfo;

        } finally {
            databaseMeta.getConnection().close();
        }
    }

    //add by zhengyi，处理索引信息
    private static void handelIndexInfo(ResultSet indexRS, List<IndexInfo> indexInfoList) throws SQLException {
        while (indexRS.next()) {
            //索引名称
            String indexName = indexRS.getString("INDEX_NAME");

            //跳过主键索引处理
            if (StringUtils.equals(indexName, "PRIMARY")) {
                continue;
            }

            //非唯一索引
            boolean nonUnique = indexRS.getBoolean("NON_UNIQUE");
            //列名
            String columnName = indexRS.getString("COLUMN_NAME");

            //记录索引信息
            addIndexInfo(indexInfoList, indexName, nonUnique, columnName);
        }
    }

    //add by zhengyi，记录单个索引信息
    private static void addIndexInfo(List<IndexInfo> indexInfoList, String indexName, boolean nonUnique, String columnName) {

        for(IndexInfo indexInfo: indexInfoList) {
            if(StringUtils.equals(indexInfo.getName(),indexName)) {
                indexInfo.setColumnList(indexInfo.getColumnList()+","+columnName);
                return;
            }
        }

        IndexInfo indexInfo = new IndexInfo();
        indexInfo.setName(indexName);
        indexInfo.setUnique(!nonUnique);
        indexInfo.setColumnList(columnName);

        indexInfoList.add(indexInfo);
    }

    private static final String columnName(ResultSet rs) {
        try {
            return rs.getString("COLUMN_NAME");
        } catch (SQLException e) {
            return null;
        }
    }

    private static String extractSchema(String schemaAndTable) {
        if (schemaAndTable.contains(".")) {
            return schemaAndTable.split(".")[0];
        } else {
            return null;
        }
    }

    private static String extractTabeName(String schemaAndTable) {
        if (schemaAndTable.contains(".")) {
            return schemaAndTable.split(".")[1];
        } else {
            return schemaAndTable;
        }
    }

}
