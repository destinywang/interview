package org.destiny;

import java.util.Stack;

/**
 * @author destiny
 * destinywk@163.com
 * -------------------------------------------
 * <p></p>
 * -------------------------------------------
 * design by 2018/9/12 20:18
 * @version 1.8
 * @since JDK 1.8.0_101
 */
public class Solution {

    public Node listSum(Node list1, Node list2) {
        // step 1: 逆置链表
        Node newList1 = preView(list1);
        Node newList2 = preView(list2);

        Node head = null, tail = null, curr = null;

        // step 2: 链表相加
        Node p = newList1, q = newList2;
        int t = 0;
        while (p != null || q != null) {
            // 直到两个链表全部遍历完成
            int valP = p == null ? 0 : p.val;
            int valQ = q == null ? 0 : q.val;
            // 当前位数的和必须为个位, 进位需要赋值给 t
            int cnt = (valP + valQ + t) % 10;
            // 进位
            t = (valP + valQ) / 10;
            curr = new Node(cnt);
            if (head == null) {
                // 当前元素
                // 如果是第一个元素
                tail = curr;
                head = curr;
            }
            tail.next = curr;
            tail = curr;

            q = q.next;
            p = p.next;
        }

        return preView(head);
    }

    /**
     * 链表逆置
     * @param head
     * @return
     */
    private Node preView(Node head) {
        Node prev = null;
        Node curr = head;

        while (curr != null) {
            Node next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
            next = curr.next;
        }

        return prev;
    }

}

class Node {
    int val;
    Node next;

    public Node(int val) {
        this.val = val;
        next = null;
    }
}