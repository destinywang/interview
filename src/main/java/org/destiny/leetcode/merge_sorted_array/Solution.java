package org.destiny.leetcode.merge_sorted_array;

import java.util.Arrays;

/**
 * @author destiny
 * destinywk@163.com
 * -------------------------------------------
 * <p></p>
 * -------------------------------------------
 * design by 2018/9/17 19:40
 * @version 1.8
 * @since JDK 1.8.0_101
 */
public class Solution {
    public void merge(int[] nums1, int m, int[] nums2, int n) {
        int length = m + n;
        int p = m - 1;
        int q = n - 1;
        for (int i = length - 1; i >= 0; --i) {
            if (p < 0) {
                nums1[i] = nums2[q--];
            } else if(q < 0) {
                nums1[i] = nums1[p--];
            } else {
                nums1[i] = nums1[p] > nums2[q] ? nums1[p--] : nums2[q--];
            }
        }
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        int[] nums1 = {1, 2, 3, 0, 0, 0};
        solution.merge(nums1, 3, new int[]{2, 5, 6}, 3);
        System.out.println(Arrays.toString(nums1));
    }
}
