package com.moon.resume;

public class SafePhone {

    public static void main(String[] args) {
        SafePhone safePhone = new SafePhone();
        String res = safePhone.mask("15605562445", 4, 7, '*');
        System.out.println(res);
    }

    /**
     * 代码中的类名、方法名、参数名已经指定，请勿修改，直接返回方法规定的值即可
     *
     *
     * @param originStr string字符串 原字符串
     * @param startIndex int整型 起始位置
     * @param endIndex int整型 结束位置
     * @param targetChar char字符型 用于替换的字符
     * @return string字符串
     */
    public String mask (String originStr, int startIndex, int endIndex, char targetChar) {
        StringBuilder stringBuilder = new StringBuilder();
        int length = originStr.length();
        for (int i = 0; i < length; i++) {
            char ch = originStr.charAt(i);
            if (i < startIndex - 1) {
                stringBuilder.append(ch);
            }
            else if (i > endIndex - 1) {
                stringBuilder.append(ch);
            }
            else {
                stringBuilder.append(targetChar);
            }
        }
        return stringBuilder.toString();
    }
}
