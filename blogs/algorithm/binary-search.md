```java
public class BinarySearch<T> {
    
    static int binarySearch(T arr[], int n, T target) {
        // 左右边界
        int l = 0, r = n - 1;
        // 只要还有可查找的内容, 就继续循环
        // 由于 l 和 r 是一个左闭右闭的区间, [l ... r], 因此 l = r 时还有元素
        while (l < r) {
            int mid = (l + r) / 2;
            if (target == arr[mid]) {
                return mid;
            } else if (target > arr[mid]) {
                // 更新左边界, 维护循环不变量
                l = mid + 1;
            } else if (target > arr[mid]) {
                // 更新右边界, 维护循环不变量
                r = mid - 1;
            }
            return -1;
        }
    }
}
```