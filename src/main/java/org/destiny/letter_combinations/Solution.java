package org.destiny.letter_combinations;


import java.util.ArrayList;
import java.util.List;

/**
 * digits 是数字字符串
 * s(digits) 是 digits 所能代表的字母字符串
 * s(digits[0 ... n - 1])
 *		= letter(digits[0]) + s(digits[1 ... n - 1])
 *		= letter(digits[0]) + s(digits[1]) + s(digits[2 ... n - 1])
 */
class Solution {

    List<String> res = new ArrayList<>();

    private static final String[] letterMap = new String[] {
            " ",         // 0
            "",         // 1
            "abc",      // 2
            "def",      // 3
            "ghi",      // 4
            "jkl",      // 5
            "mno",      // 6
            "pqrs",     // 7
            "tuv",      // 8
            "wxyz"      // 9
    };

    /**
     *
     * @param digits    处理的数字字符串
     * @param index     当前处理的位数索引
     * @param s         每次处理一个字符的时候, 之前的数字的转换翻译得到的字符串 digits[0 ... index - 1]
     */
	private void findCombination(String digits, int index, String s) {
	    if (index == digits.length()) {
	        res.add(s);
        }
        char c = digits.charAt(index);
        String letters = letterMap[c - '0'];
        for (int i = 0; i < letters.length(); ++i) {
            // 继续向下搜索
            findCombination(digits, index + 1, s + String.valueOf(letters.charAt(i)));
        }
	}
	
	public List<String> letterCombinations(String digits) {
		findCombination(digits, 0, "");
		return res;
	}
}
