/**
 * Java8Compatibility - Java 8兼容工具类
 * 
 * 提供Java 11+特性的向后兼容实现
 * 主要用于String.repeat()和String.padEnd()等方法
 * 
 * @author Java Learning Tutorial
 * @version 1.0
 * @date 2024
 */

public class Java8Compatibility {
    
    /**
     * 字符串重复方法 - 兼容Java 8
     * 模拟Java 11的String.repeat()方法
     * 
     * @param str 要重复的字符串
     * @param times 重复次数
     * @return 重复后的字符串
     */
    public static String repeat(String str, int times) {
        if (times <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    /**
     * 字符串右填充方法 - 兼容Java 8
     * 模拟Java 11的String.padEnd()方法
     * 
     * @param str 原始字符串
     * @param totalLength 目标总长度
     * @param padStr 填充字符串
     * @return 填充后的字符串
     */
    public static String padEnd(String str, int totalLength, String padStr) {
        if (str.length() >= totalLength) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < totalLength) {
            sb.append(padStr);
        }
        return sb.toString();
    }
    
    /**
     * 字符串左填充方法 - 兼容Java 8
     * 模拟Java 11的String.padStart()方法
     * 
     * @param str 原始字符串
     * @param totalLength 目标总长度
     * @param padStr 填充字符串
     * @return 填充后的字符串
     */
    public static String padStart(String str, int totalLength, String padStr) {
        if (str.length() >= totalLength) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < totalLength - str.length()) {
            sb.append(padStr);
        }
        sb.append(str);
        return sb.toString();
    }
    
    /**
     * 测试工具方法
     */
    public static void main(String[] args) {
        System.out.println("Java 8兼容性测试:");
        System.out.println("repeat('=', 20): " + repeat("=", 20));
        System.out.println("padEnd('测试', 10, ' '): '" + padEnd("测试", 10, " ") + "'");
        System.out.println("padStart('123', 6, '0'): '" + padStart("123", 6, "0") + "'");
    }
}