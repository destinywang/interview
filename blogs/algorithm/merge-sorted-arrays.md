# 1. 题目
> Given two sorted integer arrays nums1 and nums2, merge nums2 into nums1 as one sorted array.  
Node:

- The number of elements initialized in nums1 and nums2 are m and n respectively.
- You may assume that nums1 has enough space (size that is greater or equal to m + n) to hold additional elements from nums2.

---

> 给定两个有序整数数组 nums1 和 nums2，将 nums2 合并到 nums1 中，使得 num1 成为一个有序数组。  
说明:

- 初始化 nums1 和 nums2 的元素数量分别为 m 和 n。
- 你可以假设 nums1 有足够的空间（空间大小大于或等于 m + n）来保存 nums2 中的元素。


    输入:
    nums1 = [1,2,3,0,0,0], m = 3
    nums2 = [2,5,6],       n = 3

    输出: [1,2,2,3,5,6]


# 2. 解法
> 因为最后合并结果要放在 nums1 中，所以我们不能从头开始归并，这样可能会覆盖 nums1 的部分元素, 所以我们选择从末尾开始归并. 因为合并后的长度很好计算, 所以只要倒序遍历两个数组, 每次取较大的放入归并后数组应该放的位置就行了. 


```java
class Solution {
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
}
```