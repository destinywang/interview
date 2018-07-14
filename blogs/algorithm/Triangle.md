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

## 1.1 递归解法

    1. 前 n 行最小路径和 = 当前值 + 符合上下相邻约束的前(n - 1)行最小路径和
    2. 前 1 行最小路径 = triangle 第一行唯一一个元素
    
```java
class Solution {

    /**
     * 前 n 行最小路径和 = 当前值 + 符合上下相邻约束的前(n - 1)行最小路径和
     *
     * @param triangle      原三角
     * @param deep          当前处理行数
     * @param lastIndex     上一行的下标, 用于约束本行可选下标
     * @return
     */
    private int getMinimumNum(List<List<Integer>> triangle, int deep, int lastIndex) {

        // 递归返回条件: 前 0 行的最小路径和是当前唯一的元素
        if (deep == 0) {
            return triangle.get(deep).get(0);
        }
        int length = triangle.get(deep).size();

        int minIdx1 = (lastIndex - 1) < 0 ? 0 : (lastIndex - 1);
        int minIdx2 = lastIndex > (length - 1) ? length - 1 : lastIndex;
        int rst = Math.min(triangle.get(deep).get(minIdx1) + getMinimumNum(triangle, deep - 1, minIdx1),
                triangle.get(deep).get(minIdx2) + getMinimumNum(triangle, deep - 1, minIdx2));
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
            return triangle.get(deep).get(0);
        }

        int rst = Integer.MAX_VALUE;
        for (int i = 0; i < triangle.get(deep).size(); ++i) {
            rst = Math.min(rst, triangle.get(deep).get(i) + getMinimumNum(triangle, deep - 1, i));
        }

        return rst;
    }

    public int minimumTotal(List<List<Integer>> triangle) {
        return getMinimumTotal(triangle, triangle.size() - 1);
    }

}
```
## 1.2 记忆化搜索
    
我们通过分析递归解法的详细步骤, 不难得到以下信息:

    递归解法中, 有大量的重复计算过程
    
![](http://oetw0yrii.bkt.clouddn.com/18-7-14/70353808.jpg)

可以通过开辟一块空间, 专门用来缓存计算的结果, 等下轮计算再开启的时候, 如果缓存中有相应的结果, 则直接使用.

对本题而言, 最合适的方式应该是使用一块二维数组进行缓存, 二维数组的行和列分别对应 `triangle` 中的行和列, 缓存中的值则对应该行该列的最小路径和

```java
int memory[][] = new int[triangle.size()][triangel.get(triangle.size() - 1).size()];

memory:
X X X X
X X X X
X X X X
X X X X
```

我在这里使用了 HashMap<String, Integer>, 将行和列的信心组装进 key 中, 将该行该列的最小路径和存入 value, 这样的写法比较简单, 但是效率相比二维数组会有所下降.


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
        // 组装key
        String key = "" + deep + "," + lastIndex;

        // 如果可以 存在, 直接返回
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
        // 将得到的结果缓存进 map 中
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