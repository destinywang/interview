package org.destiny.leetcode.two_sum;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author destiny
 * destinywk@163.com
 * -------------------------------------------
 * <p></p>
 * -------------------------------------------
 * design by 2018/9/17 20:19
 * @version 1.8
 * @since JDK 1.8.0_101
 */
public class Solution {

    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        int[] res = new int[2];
        for(int i = 0; i < nums.length; ++i) {
            Integer t = map.get(nums[i]);
            if(t == null) {
                // 对应表中没有结果
                map.put(target - nums[i], i);
            } else {
                res[0] = t;
                res[1] = i;
                return res;
            }
        }
        return res;
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        int[] ints = solution.twoSum(new int[]{2, 7, 11, 15}, 9);
        System.out.println(Arrays.toString(ints));
    }
}
