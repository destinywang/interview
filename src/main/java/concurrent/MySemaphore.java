package concurrent;

import java.util.concurrent.Semaphore;

/**
 * @author 王康
 * hzwangkang1@corp.netease.com
 * ------------------------------------------------------------------
 * <p></p>
 * ------------------------------------------------------------------
 * Corpright 2018 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * @version JDK 1.8.0_101
 * @since 2018/7/7 20:48
 */
public class MySemaphore {
    public static void main(String[] args){
        System.out.println("-- Action ... GO! --");
        Semaphore semaphore = new Semaphore(3);
        for (int i = 0; i < 20; ++ i) {
            Thread thread = new Thread(new SemaphoreWorker(semaphore));
            thread.start();
        }
    }
}

class SemaphoreWorker implements Runnable {
    private String name = Thread.currentThread().getName();
    private Semaphore semaphore;
    public SemaphoreWorker(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        try{
            System.out.println(name + " is waiting for a permit");
            semaphore.acquire();
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            System.out.println(name + " released a permit");
            semaphore.release();
        }
    }
}