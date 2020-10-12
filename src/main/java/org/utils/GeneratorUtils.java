package org.utils;

public final class GeneratorUtils {

    private GeneratorUtils() {}

    public static String generateNbsp(int num) {
        if (num < 1) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < num; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

    public static String toCamelString(String str) {
        if (str == null) return "";
        char[] chars = str.toCharArray();
        if (chars[0] >= 'a' && chars[0] <= 'z') {
            chars[0] -= 32;
        }
        return new String(chars);
    }
}
