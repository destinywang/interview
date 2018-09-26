package org.destiny.leetcode.valid_palindrome;

/**
 * @author destiny
 * destinywk@163.com
 * ------------------------------------------------------------------
 * <p>
 *     判断一个字符串是不是回文字符串, 同时需要忽略掉所有非子母数字的字符
 * </p>
 * ------------------------------------------------------------------
 * design by 2018/9/17 23:33
 * @version 1.8
 * @since JDK 1.8.0_101
 */
public class Solution {
    public boolean isPalindrome(String s) {
        if ("".equals(s)) {
            return true;
        }
        s = s.toUpperCase();
        int low = 0;
        int high = s.length() - 1;
        boolean flag = true;
        while(low < high) {
            while(!judgeChar(s, low)) {
                ++low;
            }
            while(!judgeChar(s, high)) {
                --high;
            }
            System.out.println(s.charAt(low) + " - " + s.charAt(high));
            if (s.charAt(low) != s.charAt(high)) {
                flag = false;
            }
            ++low;
            --high;
        }
        return flag;
    }

    private boolean judgeChar(String s, int index) {
        char c = s.charAt(index);
        return (c <= 'z' && c >= 'A') || (c >= '0' && c <= '9');
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        boolean b = solution.isPalindrome("A man, a plan, a canal: Panama");
        System.out.println(b);
    }
}
