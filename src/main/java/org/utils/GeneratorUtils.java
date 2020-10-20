package org.utils;

public final class GeneratorUtils {

    private GeneratorUtils() {}

    /**
     * 生成指定数量的连续空格
     * @param num 要生成的空格数量
     * @return 生成结果字符串
     */
    public static String generateNbsp(int num) {
        if (num < 1) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < num; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

    /**
     * 将单词首字母大写
     * @param str 单词
     * @return 首字母大写的单词
     */
    public static String toCamelString(String str) {
        if (str == null) return "";
        char[] chars = str.toCharArray();
        if (chars[0] >= 'a' && chars[0] <= 'z') {
            chars[0] -= 32;
        }
        return new String(chars);
    }

    /**
     * 类型处理：将char型数组转为String型数组
     * @param src char型数组
     * @return String型数组
     */
    public static String[] charArrayToStringArray(char[] src) {
        if (src == null) {
            throw new NullPointerException("Array 'src' and 'tgt' cannot be null!");
        }
        int len = src.length;
        String[] result = new String[len];
        for (int i = 0; i < len; i++) {
            result[i] = String.valueOf(src[i]);
        }
        return result;
    }

    /**
     * 将实体类的字段名变为数据表的列名(驼峰->下划线)
     * @param fieldName 实体类的字段名
     * @return 数据表的列名
     */
    public static String fieldNameToColName(String fieldName) {
        if (fieldName == null) return "";
        char[] charArr = fieldName.toCharArray();
        int len = charArr.length;
        String[] strArr = GeneratorUtils.charArrayToStringArray(charArr);
        for (int i = 0; i < len; i++) {
            char ch = strArr[i].charAt(0);
            if (ch >= 'A' && ch <= 'Z') {
                strArr[i] = "_" + (char) (ch + 32);
            }
        }
        StringBuilder builder = new StringBuilder();
        for (String s : strArr) {
            builder.append(s);
        }
        return builder.toString();
    }

}
