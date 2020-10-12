package org.generator;

import org.annotation.Nullable;
import org.annotation.PrimaryKey;
import org.annotation.Unique;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

public final class SqlTableGenerator {

    private SqlTableGenerator() {}

    private static final Properties dbProps = new Properties();
    private static final int DEFAULT_VARCHAR_LENGTH = 31;
    private static final HashMap<String, String> javaTypeToJdbcType = new HashMap<>();

    static {
        try {
            FileReader reader = new FileReader("classpath:db.properties");
            dbProps.load(reader);
            javaTypeToJdbcType.put("Integer", "INT");
            javaTypeToJdbcType.put("String", "VARCHAR(" + DEFAULT_VARCHAR_LENGTH + ")");
            javaTypeToJdbcType.put("Byte", "TINYINT");
            javaTypeToJdbcType.put("Short", "SMALLINT");
            javaTypeToJdbcType.put("Long", "BIGINT");
            javaTypeToJdbcType.put("Double", "DOUBLE");
            javaTypeToJdbcType.put("Float", "FLOAT");
            javaTypeToJdbcType.put("Character", "CHAR(1)");
            javaTypeToJdbcType.put("Boolean", "INT(1)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean createTable(Class<?> entityClass) {
        String url = (String) dbProps.get("url");
        String username = (String) dbProps.get("username");
        String password = (String) dbProps.get("password");
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("CREATE TABLE IF NOT EXISTS ").append(entityClass.getSimpleName().toLowerCase()).append(" (");
            Field[] fields = entityClass.getDeclaredFields();
            for (Field f : fields) {
                sqlBuilder.append(fieldNameToColName(f.getName())).append(' ').append(javaTypeToJdbcType.get(f.getType().getSimpleName())).append(' ');
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
        } catch (SQLException e) {
            e.printStackTrace(System.out);
            return false;
        }
        return false;
    }

    private static String fieldNameToColName(String fieldName) {
        if (fieldName == null) return "";
        char[] charArr = fieldName.toCharArray();
        int len = charArr.length;
        String[] strArr = new String[len];
        System.arraycopy(charArr, 0, strArr, 0, len);
        for (int i = 0; i < len; i++) {
            char ch = strArr[i].charAt(0);
            if (ch >= 'A' && ch <= 'Z') {
                strArr[i] = "_" + (ch + 32);
            }
        }
        StringBuilder builder = new StringBuilder();
        for (String s : strArr) {
            builder.append(s);
        }
        return builder.toString();
    }

}
