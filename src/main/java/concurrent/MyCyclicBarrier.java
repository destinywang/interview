package concurrent;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyCyclicBarrier {

    static class Runner implements Runnable {
        private CyclicBarrier barrier;
        private String name;

        public Runner(CyclicBarrier barrier, String name) {
            this.barrier = barrier;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                int seconds = new Random().nextInt(10);
                Thread.sleep(1000 * seconds);
                System.out.println(name + " 经过 " + seconds + " 秒 准备OK ");
                barrier.await();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(name + " GO");
        }

        public static void main(String[] args) {
            CyclicBarrier barrier = new CyclicBarrier(3);
            ExecutorService executorService = Executors.newFixedThreadPool(3);

            executorService.submit(new Thread(new Runner(barrier, "destiny1")));
            executorService.submit(new Thread(new Runner(barrier, "destiny2")));
            executorService.submit(new Thread(new Runner(barrier, "destiny3")));

            executorService.shutdown();
        }
    }
}