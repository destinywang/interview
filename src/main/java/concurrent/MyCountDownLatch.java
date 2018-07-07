package concurrent;

import java.util.concurrent.CountDownLatch;

/**
 * @author 王康
 * hzwangkang1@corp.netease.com
 * ------------------------------------------------------------------
 * <p></p>
 * ------------------------------------------------------------------
 * Corpright 2018 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * @version JDK 1.8.0_101
 * @since 2018/7/7 21:10
 */
public class MyCountDownLatch {

    public static void main(String[] args) {

        // countDownLatch只需要等待两个线程执行countDown操作即可继续执行
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        Thread t1 = new Thread(() -> {
            try {
                System.out.println("进入线程t1" + "等待其他线程处理完成");
                countDownLatch.await();
                System.out.println("t1线程继续执行");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            try {
                System.out.println("t2线程进行初始化操作");
                Thread.sleep(3000);
                System.out.println("t2线程初始化完毕，通知t1继续执行");
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t2");

        Thread t3 = new Thread(() -> {
            try {
                System.out.println("t3线程进入初始化操作");
                Thread.sleep(4000);
                System.out.println("t3线程初始化完成，通知t1继续执行");
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t3");

        t1.start();
        t2.start();
        t3.start();
    }
}

