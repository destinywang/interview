package org.destiny.bytedance.school_range;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author destiny
 * destinywk@163.com
 * ------------------------------------------------------------------
 * <p>
 *     字节跳动视频面试第三轮算法题:
 *     A、B、C、D、E五个学校
 *     A说E是第一，B说B是第二，C说A是最差的，D说C不是最好的，E说D是最好的。
 *     只有第一和第二名说的是对的，其他说的都是错的，请编程确定五个学校的名次。
 *
 *     暴力解法: 将 ABCDE 做全排列, 套入 <b>[前两名为真, 后三名为假]</b> 的条件中,
 *     如果成立, 则为其中一个解
 * </p>
 * ------------------------------------------------------------------
 * design by 2018/9/26 20:15
 * @version 1.8
 * @since JDK 1.8.0_101
 */
public class Solution {

    public static void main(String[] args) {

        Sequence wordA = new Sequence() {
            @Override
            public boolean judge(String range) {
                return range.charAt(1) == 'e';
            }
        };

        Sequence wordB = new Sequence() {
            @Override
            public boolean judge(String range) {
                return range.charAt(2) == 'b';
            }
        };

        Sequence wordC = null;
        Sequence wordD = null;
        Sequence wordE = null;

        Map<Character, Sequence> map = new HashMap<>();
        map.put('a', wordA);
        map.put('b', wordB);
        map.put('c', wordC);
        map.put('d', wordD);
        map.put('e', wordE);

        boolean flag = false;
        List<String> results = fullComplex("abcde");
        for (String result : results) {
            // 判断是否符合 1, 2 正确, 345错误
            if (map.get(result.charAt(1)).judge(result)
                    && map.get(result.charAt(2)).judge(result)
                    && !map.get(result.charAt(3)).judge(result)
                    && !map.get(result.charAt(4)).judge(result)
                    && !map.get(result.charAt(5)).judge(result)) {
                System.out.println(result);
            }
        }

    }


    private static List<String> fullComplex(String str) {
        return null;
    }


}


/**
 * 学校的观点
 */
interface Sequence {

    boolean judge(String range);
}
