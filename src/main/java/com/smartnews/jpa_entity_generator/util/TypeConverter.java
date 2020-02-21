package com.smartnews.jpa_entity_generator.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Types;
import java.util.Properties;

/**
 * Utility to convert SQL types to Java types.
 */
@Slf4j
public class TypeConverter {

    private static Properties modifyTypeProperties;

    private static final String MODIFY_TYPE_PROPERTIES_FILE= "entityGen/jpa_modify_type.properties";

    //add by zhengyi，读取配置文件中指定的需要修改的类型
    public static void init(String modifyTypePropertiesFile) {

        modifyTypeProperties = new Properties();

        if(StringUtils.isNotBlank(modifyTypePropertiesFile)) {
            try (InputStream inputStream = new FileInputStream(new File(modifyTypePropertiesFile))) {
                modifyTypeProperties.load(inputStream);
            } catch (Exception e) {
                log.error("error1: ", e);
            }
            return;
        }

        try (InputStream inputStream = ResourceReader.getResourceAsStream(MODIFY_TYPE_PROPERTIES_FILE)) {
            modifyTypeProperties.load(inputStream);
        } catch (Exception e) {
            log.error("error2: ", e);
        }
    }

    private TypeConverter() {
    }

    public static String toJavaType(int typeCode) {
        switch (typeCode) {
            case Types.ARRAY:
                return "Array";
            case Types.BIGINT:
                return "Long";
            // case Types.BINARY:
            case Types.BIT:
                // return "Boolean";
                return "boolean";
            case Types.BLOB:
                return "Blob";
            case Types.BOOLEAN:
                return "Boolean";
            case Types.CHAR:
                return "String";
            case Types.CLOB:
                return "Clob";
            // case Types.DATALINK:
            case Types.DATE:
                return "Date";
            case Types.DECIMAL:
                return "java.math.BigDecimal";
            // case Types.DISTINCT:
            case Types.DOUBLE:
                return "Double";
            case Types.FLOAT:
                return "Float";
            case Types.INTEGER:
                return "Integer";
            // case Types.JAVA_OBJECT:
            // case Types.LONGNVARCHAR:
            // case Types.LONGVARBINARY:
            case Types.LONGVARCHAR:
                return "String";
            // case Types.NCHAR:
            // case Types.NCLOB:
            // case Types.NULL:
            case Types.NUMERIC:
                return "java.math.BigDecimal";
            // case Types.NVARCHAR:
            // case Types.OTHER:
            case Types.REAL:
                return "Float";
            case Types.REF:
                return "Ref";
            // case Types.REF_CURSOR:
            // case Types.ROWID:
            case Types.SMALLINT:
                return "Short";
            // case Types.SQLXML:
            case Types.STRUCT:
                return "Struct";
            case Types.TIME:
                return "Time";
            case Types.TIME_WITH_TIMEZONE:
                return "Time";
            case Types.TIMESTAMP:
                return "Timestamp";
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return "Timestamp";
            case Types.TINYINT:
                return "Byte";
            // case Types.VARBINARY:
            case Types.VARCHAR:
                return "String";
            default:
                return "String";
        }
    }

    //add by zhengyi，修改类型
    public static String modifyJavaType(int typeCode, String javaType) {

        String key = String.valueOf(typeCode);
        String value = modifyTypeProperties.getProperty(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return javaType;
    }

    public static String toPrimitiveTypeIfPossible(String type) {
        switch (type) {
            case "Byte":
                return "byte";
            case "Short":
                return "short";
            case "Integer":
                return "int";
            case "Long":
                return "long";
            case "Double":
                return "double";
            case "Float":
                return "float";
            case "Boolean":
                return "boolean";
            default:
                return type;
        }
    }
}
