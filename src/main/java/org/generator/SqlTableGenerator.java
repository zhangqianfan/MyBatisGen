package org.generator;

import org.annotation.Nullable;
import org.annotation.PrimaryKey;
import org.annotation.Unique;
import org.connection.DBConnection;
import org.utils.GeneratorUtils;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.HashMap;

public final class SqlTableGenerator {

    private SqlTableGenerator() {}

    private static final String RESOURCES_PATH = "src/main/resources/";
    private static final String DB_CONF_FILE_PATH = RESOURCES_PATH + "db.properties";
    private static final int DEFAULT_INT_LENGTH = 4;
    private static final int DEFAULT_VARCHAR_LENGTH = 32;
    private static final HashMap<String, String> javaTypeToJdbcType = new HashMap<>();

    static {
        javaTypeToJdbcType.put("Integer", "INT(" + DEFAULT_INT_LENGTH + ")");
        javaTypeToJdbcType.put("String", "VARCHAR(" + DEFAULT_VARCHAR_LENGTH + ")");
        javaTypeToJdbcType.put("Byte", "TINYINT");
        javaTypeToJdbcType.put("Short", "SMALLINT");
        javaTypeToJdbcType.put("Long", "BIGINT");
        javaTypeToJdbcType.put("Double", "DOUBLE");
        javaTypeToJdbcType.put("Float", "FLOAT");
        javaTypeToJdbcType.put("Character", "CHAR(1)");
        javaTypeToJdbcType.put("Boolean", "INT(1)");
    }

    /**
     * 根据实体类创建对应的数据库表
     * @param entityClass 要创建数据库表的实体类
     * @return 数据表创建成功返回true，否则返回false
     */
    public static boolean createTable(Class<?> entityClass) {
        try {
            DBConnection dbConn = new DBConnection(DB_CONF_FILE_PATH);
            Connection connection = dbConn.getConnection();
            if (connection == null) {
                throw new NullPointerException("connection cannot be null!");
            }
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("CREATE TABLE IF NOT EXISTS ").append(entityClass.getSimpleName().toLowerCase()).append(" (");
            Field[] fields = entityClass.getDeclaredFields();
            for (Field f : fields) {
                sqlBuilder.append(GeneratorUtils.fieldNameToColName(f.getName())).append(' ').append(javaTypeToJdbcType.get(f.getType().getSimpleName())).append(' ');
                PrimaryKey pkAnn = f.getAnnotation(PrimaryKey.class);
                Unique uniqueAnn = f.getAnnotation(Unique.class);
                Nullable nullableAnn = f.getAnnotation(Nullable.class);
                if (pkAnn != null) {
                    sqlBuilder.append("PRIMARY KEY, ");
                } else if (uniqueAnn != null) {
                    sqlBuilder.append("UNIQUE");
                    if (nullableAnn == null) {
                        sqlBuilder.append(" NOT NULL, ");
                    } else {
                        sqlBuilder.append(", ");
                    }
                } else {
                    if (nullableAnn == null) {
                        sqlBuilder.append("NOT NULL, ");
                    } else {
                        sqlBuilder.append(", ");
                    }
                }
            }
            sqlBuilder.replace(sqlBuilder.lastIndexOf(", "), sqlBuilder.length(), ")");
            String sql = sqlBuilder.toString();
            PreparedStatement ps = connection.prepareStatement(sql);
            int rows = ps.executeUpdate();
            return rows == 0;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return false;
        }
    }

}
