package org.destiny.daffodils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author destiny
 * destinywk@163.com
 * -------------------------------------------
 * <p></p>
 * -------------------------------------------
 * design by 2018/9/16 20:15
 * @version 1.8
 * @since JDK 1.8.0_101
 */
public class Solution {

    public boolean daffodils(int num) {
        int t = num;
        List<Integer> numbers = new ArrayList<>();
        while (num > 0) {
            numbers.add(num % 10);
            num /= 10;
        }

        int sumOfPow3 = 0;
        for (Integer number : numbers) {
            sumOfPow3 += powOf3(number);
        }
        return sumOfPow3 == t;
    }

    private int powOf3(int num) {
        return num * num * num;
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        System.out.println(solution.daffodils(153));
    }
}
