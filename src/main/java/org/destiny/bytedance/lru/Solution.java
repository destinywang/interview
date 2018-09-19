package org.destiny.bytedance.lru;

import java.util.HashMap;
import java.util.Map;

/**
 * @author destiny
 * destinywk@163.com
 * ------------------------------------------------------------------
 * <p>
 *     字节跳动视频面试二面题目
 *     设计一个 LRU 算法, 保证该 LRU 的 get() 和 set() 操作时间复杂度都为 O(1)
 *     同时要求当 LRU 超过一定的长度后, 需要将末端的数据删除
 * </p>
 * ------------------------------------------------------------------
 * design by 2018/9/18 21:46
 * @version 1.8
 * @since JDK 1.8.0_101
 */
public class Solution {

    private static final int CAPACITY = 100;
    private static int CURR_CNT = 0;

    private Map<String, Node> memHash = new HashMap<>();

    private Node head = null;

    private Node tail = null;

    /**
     * 根据 key 值, 返回对应对象, 同时更新 LRU 结构
     * @param key
     * @return
     */
    public Node get(String key) {
        if (key == null || head == null) {
            return null;
        }
        Node node = memHash.get(key);
        if (node == null) {
            return null;
        }
        // 将给节点移动到头部
        boolean b = false;
        if (node != head) {
             b = moveHead(node);
        }
        // 省去对移动结果的处理
        return node;
    }

    public Node set(String key) {
        if (head == null) {
            // head 为空, 初始化队列
            head = new Node(key);
            tail = head;
            ++CURR_CNT;
            memHash.put(key, head);
            return head;
        } else {
            // head 不为空, 需要进行头插
            // step1: 头插
            insertHead(key);
            ++CURR_CNT;
            memHash.put(key, head);
            // step2: 检查元素数量
            if (CURR_CNT > CAPACITY) {
                // 如果已经超出最大数量, 需要进行尾删
                tailDelete();
            }
        }
        return head;
    }

    /**
     * 头插
     * @param key
     * @return
     */
    private void insertHead(String key) {
        if (head == null) {
            try {
                throw new IllegalAccessException("当前 LRU 队列为空, 无法操作");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Node newHead = new Node(key);
        newHead.next = head;
        head.prev = newHead;
        head = newHead;
    }

    /**
     * 尾删
     * @return
     */
    private boolean tailDelete() {
        try {
            tail = tail.prev;
            tail.next = null;
            --CURR_CNT;
            return true;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将指定节点移动到头部
     * @param t
     * @return
     */
    private boolean moveHead(Node t) {
        try {
            if (t == tail) {
                tail = tail.prev;
                t.prev.next = null;
            } else {
                t.prev.next = t.next;
            }
            insertHead(t.key);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

}

class Node {
    String key;
    Node prev;
    Node next;

    public Node(String key) {
        this.key = key;
        prev = null;
        next = null;
    }
}
