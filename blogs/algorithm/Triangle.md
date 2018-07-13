# 1. Triangle

出自 Leetcode 第 120 题

    Given a triangle, find the minimum path sum from top to bottom. Each step you may move to adjacent numbers on the row below.

    For example, given the following triangle
    
        [
         [2],
        [3,4],
       [6,5,7],
      [4,1,8,3]
    ]
    
    The minimum path sum from top to bottom is 11 (i.e., 2 + 3 + 5 + 1 = 11).
    
这一道比较经典动态规划问题;

## 1.1 可以尝试使用如下思路得出其递归解法:

    1. 前 n 行最小路径和 = 当前值 + 符合上下相邻约束的前(n - 1)行最小路径和
    2. 前 1 行最小路径 = triangle 第一行唯一一个元素
    
因此可以写出如下代码
```java
class Solution {

    /**
     * 记忆化搜索
     */
    private Map<String, Integer> memory;

    /**
     * 前 n 行最小路径和 = 当前值 + 符合上下相邻约束的前(n - 1)行最小路径和
     *
     * @param triangle      原三角
     * @param deep          当前处理行数
     * @param lastIndex     上一行的下标, 用于约束本行可选下标
     * @return
     */
    private int getMinimumNum(List<List<Integer>> triangle, int deep, int lastIndex) {
        String key = "" + deep + "," + lastIndex;

        if (memory.get(key) != null) {
            return memory.get(key);
        }

        // 递归返回条件: 前 0 行的最小路径和是当前唯一的元素
        if (deep == 0) {
            return triangle.get(deep).get(0);
        }
        int length = triangle.get(deep).size();

        int minIdx1 = (lastIndex - 1) < 0 ? 0 : (lastIndex - 1);
        int minIdx2 = lastIndex > (length - 1) ? length - 1 : lastIndex;
        int rst = Math.min(triangle.get(deep).get(minIdx1) + getMinimumNum(triangle, deep - 1, minIdx1),
                triangle.get(deep).get(minIdx2) + getMinimumNum(triangle, deep - 1, minIdx2));
        memory.put(key, rst);
        return rst;
    }

    /**
     * 前 n 行最小路径 = 第 n 行最小元素 + 前 (n - 1) 行最小路径
     * 其中相邻两行必须遵循特定条件: 每一步只能移动到下一行中相邻的结点上。
     * 对于下一行来说, 只能考虑上一行的 n 和 n + 1 两个节点
     * 对于上一行来说, 只能考虑下一行的 n - 1 和 n 两个节点
     *
     * @param triangle
     * @param deep
     * @return
     */
    private int getMinimumTotal(List<List<Integer>> triangle, int deep) {

        if (deep == 0) {
            memory.put("" + deep + 0, 0);
            return triangle.get(deep).get(0);
        }

        int rst = Integer.MAX_VALUE;
        for (int i = 0; i < triangle.get(deep).size(); ++i) {
            rst = Math.min(rst, triangle.get(deep).get(i) + getMinimumNum(triangle, deep - 1, i));
        }

        return rst;
    }

    public int minimumTotal(List<List<Integer>> triangle) {
        memory = new HashMap<>();
        return getMinimumTotal(triangle, triangle.size() - 1);
    }
}

```