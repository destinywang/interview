package org.destiny.leetcode.preorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author destiny
 * destinywk@163.com
 * -------------------------------------------
 * <p>
 *     前序打印二叉树
 * </p>
 * -------------------------------------------
 * design by 2018/9/12 15:10
 * @version 1.8
 * @since JDK 1.8.0_101
 */
public class Solution {

    public static List<Integer> preOrderTraversal(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) {
            return result;
        }
        Stack<Command> stack = new Stack<>();
        stack.push(new Command("go", root));
        while (!stack.empty()) {
            Command command = stack.pop();
            // 分析栈顶 command
            if ("print".equals(command.s)) {
                // 如果是打印命令
                result.add(command.node.val);
            } else {
                if (command.node.right != null) {
                    stack.push(new Command("go", command.node.right));
                } if (command.node.left != null) {
                    stack.push(new Command("go", command.node.left));
                }
                stack.push(new Command("print", command.node));
            }
        }
        return result;
    }

    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        public TreeNode(int val) {
            this.val = val;
            left = null;
            right = null;
        }
    }

    static class Command {
        String s;           // go print
        TreeNode node;

        public Command(String s, TreeNode node) {
            this.s = s;
            this.node = node;
        }
    }

    public static void main(String[] args) {
        TreeNode a = new TreeNode(3);
        TreeNode b = new TreeNode(1);
        TreeNode c = new TreeNode(2);

        a.left = b;
        a.right = c;

        List<Integer> integers = preOrderTraversal(a);
        System.out.println(integers);
    }
}
