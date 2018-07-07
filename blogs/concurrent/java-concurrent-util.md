# 1. Java 并发包常见的工具类
- 提供了比 `synchronize` 更高级的各种同步结构, 包括 `CountDownLatch`, `CyclicBarrier`, `Semaphore` 等, 可以实现更加丰富的多线程操作
- 提供了各种线程安全的容器, 如常见的 `ConcurrentHashMap`, `ConcurrentSkipListMap`, `CopyOnWriteArrayList` 等
- 提供了并发队列实现, 如 `BlockedQueue`
- 提供了线程池框架, 可以创建各种不同类型的线程池

# 2. 同步结构
## 2.1 Semaphore

    它通过控制一定数量的允许方式, 来达到限制通用资源访问的目的
    
> 在进入机场大厅的时候, 为了防止拥挤, 保安会指挥进入的队伍一次进来 10 人, 等这 10 人安检结束, 再放进去下一批, 这与 Semaphore 的工作原理类似

```java
import java.util.concurrent.Semaphore;

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
```

同时, 如果 Semaphore 的数值初始化为 1, 那么一个线程就可以通过 `acquire` 进入互斥状态.

CountDownLatch 和 CyclicBarrier
- CountDownLatch 的不可重置的, 所以无法重用, CyclicBarrier 则没有这种限制, 可以重用
- CountDownLatch 的基本操作组合是 `countDown() / await()`, 调用 await 的线程阻塞等待 countDown 足够的次数
- CyclicBarrier 的基本操作组合是 `await()`, 当所有的伙伴都调用了 await, 才会继续执行任务, 并自动进行重置

## 2.2 CountDownLatch

    提供类似倒计时的功能, await 的线程阻塞等待 CountDownLatch 倒数结束, 然后再执行后续操作

```java
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
                System.out.println("t1线程初始化完毕，通知t1继续执行");
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
```

执行结果:

    进入线程t1等待其他线程处理完成
    t3线程进入初始化操作
    t2线程进行初始化操作
    t2线程初始化完毕，通知t1继续执行
    t3线程初始化完成，通知t1继续执行
    t1线程继续执行
    
## 2.3 CyclicBarrier

    CyclicBarrier 提供的是一个类似栅栏的功能, 必须等到所有线程都完成某一项工作之后, 再一起进行后续操作

```java
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
```

执行结果:

    进入线程t1等待其他线程处理完成
    t3线程进入初始化操作
    t2线程进行初始化操作
    t2线程初始化完毕，通知t1继续执行
    t3线程初始化完成，通知t1继续执行
    t1线程继续执行