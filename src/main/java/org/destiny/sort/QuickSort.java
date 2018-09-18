package org.destiny.sort;

import java.util.Arrays;

/**
 * @author destiny
 * destinywk@163.com
 * -------------------------------------------
 * <p></p>
 * -------------------------------------------
 * design by 2018/9/1 22:39
 * @version 1.8
 * @since JDK 1.8.0_101
 */
public class QuickSort {

    public static void quickSort(int[] array, int low, int high) {
        if (low > high) {
            return;
        }
        int i = low, j = high;
        int t = array[i];
        while (i < j) {
            while (i < j && array[j] >= t) {
                --j;
            }
            array[i] = array[j];
            while (i < j && array[i] <= t) {
                ++i;
            }
            array[j] = array[i];
        }
        array[i] = t;
        quickSort(array, low, i - 1);
        quickSort(array, i + 1, high);
    }

    public static void main(String[] args) {
        int[] ints = {6, 2, 5, 7, 1, 0, 9, 3, 4, 8, 8};
        quickSort(ints, 0, ints.length -1);
        System.out.println(Arrays.toString(ints));
    }
}
