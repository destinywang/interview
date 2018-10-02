package org.destiny.concurrent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author destiny
 * destinywk@163.com
 * ------------------------------------------------------------------
 * <p>
 * 用于测试一个进程有3个线程，如果一个线程抛出oom，其他两个线程是否还能运行
 * 的示例代码
 * </p>
 * ------------------------------------------------------------------
 * design by 2018/10/2 10:35
 * @version 1.8
 * @since JDK 1.8.0_101
 */
public class OutOfMemTest {

    public static void main(String[] args) {
        /*
         * 第一个线程, 用来实现 OOM 场景, 每秒分配 1M 内存
         */
        new Thread(() -> {
            List<byte[]> mem = new ArrayList<>();
            while (true) {
                System.out.println("== " + new Date().toString() + " : " + Thread.currentThread());
                byte[] bytes = new byte[1024 * 1024];
                mem.add(bytes);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        ).start();

        /*
         * 第二个线程, 不做任何操作, 只测试在 OOM 发生时是否能够正常执行
         */
        new Thread(() -> {
            while (true) {
                System.out.println("-- " + new Date().toString() + " : " + Thread.currentThread());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
