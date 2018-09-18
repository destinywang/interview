package org.destiny.sort;

import java.util.Arrays;

/**
 * @author destiny
 * destinywk@163.com
 * -------------------------------------------
 * <p>
 * -------------------------------------------
 * design by 2018/9/1 22:15
 * @version 1.8
 * @since JDK 1.8.0_101
 */
public class BubbleSort {

    public static void bubbleSort(int[] array, int n) {
        for (int i = 0; i < n - 1; ++i) {
            for (int j = 0; j < n - i - 1; ++j) {
                if (array[j] > array[j + 1]) {
                    int t = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = t;
                }
            }
        }
    }

    public static void main(String[] args) {
        int[] ints = {6, 2, 5, 7, 1, 0, 9, 3, 4, 8};
        bubbleSort(ints, ints.length);
        System.out.println(Arrays.toString(ints));
    }
}
