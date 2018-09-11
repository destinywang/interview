# 1. 题目
> Given an array of n positive integers and a positive integer s, find the minimal length of a contiguous subarray of which the sum ≥ s. If there isn't one, return 0 instead.
 
---

> 给定一个含有 n 个正整数的数组和一个正整数 s ，找出该数组中满足其和 ≥ s 的长度最小的连续子数组。如果不存在符合条件的连续子数组，返回 0。

    输入: s = 7, nums = [2,3,1,2,4,3]
    输出: 2
    解释: 子数组 [4,3] 是该条件下的长度最小的连续子数组。
    
# 2. 求解
```java
class Solution {
    public int minSubArrayLen(int s, int[] nums) {
        // nums[l ... r] 作为滑动窗口
        int l = 0, r = - 1;
        int sum = 0;
        int res = nums.length + 1;
        while (l < nums) {
            if (r + 1 < nums.length && numssum < s) {
                sum += nums[++r];
            } else {
                sum -= nums[l++];
            }
            if (sum >= s) {
                res = Math.min(res,  r - l + 1);
            }
        }
        
        if (res == nums.length + 1) {
            // 如果还等于初始值
            return 0;
        }
        return res;
    }
}
```