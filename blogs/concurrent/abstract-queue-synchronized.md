# 1. 什么是 AQS

    AQS(AbstractQueuedSynchronizer), AQS 是 JDK 下提供的一套用于实现基于FIFO等待队列的阻塞锁和相关的同步器的一个同步框架.
    诸如 ReentrantLock, CountDownLatch, Semaphore 等都是由 AQS 实现的.
    这个抽象类被设计为作为一些可用原子 int 值来表示状态的同步器的基类. 
    在 Java 的同步组件中, AQS 的子类一般是同步组件的静态内部类。
    
# 2. AQS 的结构

AQS 的属性:
```java
// 头结点，你直接把它当做 当前持有锁的线程 可能是最好理解的
private transient volatile Node head;
// 阻塞的尾节点，每个新的节点进来，都插入到最后，也就形成了一个隐视的链表
private transient volatile Node tail;
// 这个是最重要的，不过也是最简单的，代表当前锁的状态，0代表没有被占用，大于0代表有线程持有当前锁
// 之所以说大于0，而不是等于1，是因为锁可以重入嘛，每次重入都加上1
private volatile int state;
// 代表当前持有独占锁的线程，举个最重要的使用例子，因为锁可以重入
// reentrantLock.lock()可以嵌套调用多次，所以每次用这个来判断当前线程是否已经拥有了锁
// if (currentThread == getExclusiveOwnerThread()) {state++}
private transient Thread exclusiveOwnerThread; //继承自AbstractOwnableSynchronizer
```

AQS 内部包含一个 `阻塞队列`, 用于实现锁的特性, 例如 `共享锁/独占锁`, `公平锁/非公平锁`等

![image](https://javadoop.com/blogimages/AbstractQueuedSynchronizer/aqs-0.png)

在等待队列中, 每个线程会被包装成一个 `Node`, 多个 `Node` 之间采用链表的形式进行组织.

```java
static final class Node {
    /** Marker to indicate a node is waiting in shared mode */
    // 标识节点当前在共享模式下
    static final Node SHARED = new Node();
    /** Marker to indicate a node is waiting in exclusive mode */
    // 标识节点当前在独占模式下
    static final Node EXCLUSIVE = null;

    // ======== 下面的几个int常量是给waitStatus用的 ===========
    /** waitStatus value to indicate thread has cancelled */
    // 代码此线程取消了争抢这个锁
    static final int CANCELLED =  1;
    /** waitStatus value to indicate successor's thread needs unparking */
    // 官方的描述是，其表示当前node的后继节点对应的线程需要被唤醒
    static final int SIGNAL    = -1;
    /** waitStatus value to indicate thread is waiting on condition */
    static final int CONDITION = -2;
    /**
     * waitStatus value to indicate the next acquireShared should
     * unconditionally propagate
     */
    static final int PROPAGATE = -3;
    // =====================================================

    // 取值为上面的1、-1、-2、-3，或者0(以后会讲到)
    // 这么理解，暂时只需要知道如果这个值 大于0 代表此线程取消了等待，
    // 也许就是说半天抢不到锁，不抢了，ReentrantLock是可以指定timeouot的。。。
    volatile int waitStatus;
    // 前驱节点的引用
    volatile Node prev;
    // 后继节点的引用
    volatile Node next;
    // 这个就是线程本尊
    volatile Thread thread;
}
```

Node 的结构中, 主要包括
- thread
- waitStatus
- pre
- next
四个属性.

# 3. ReentrantLock 公平锁的实现方式
ReentrantLock 的常见使用方式:
```java
// true 代表公平锁
ReentrantLock reentrantLock = new ReentrantLock(true);

reentrantLock.lock();
// 通常，lock 之后紧跟着 try 语句
try {
    // 执行需要加锁的操作
} finally {
    // 释放锁
    reentrantLock.unlock();
}
```

在 ReentrantLock 内部, 使用了内部类 `Sync` 来管理锁, 真正的获取锁和释放锁是由 Sync 的实现类来控制的。

![](http://oetw0yrii.bkt.clouddn.com/18-7-16/91250701.jpg)

Sync 有两个实现, 分别为 NonfairSync（非公平锁）和 FairSync（公平锁），我们看 FairSync 部分。

```java
/**
 * Creates an instance of {@code ReentrantLock} with the
 * given fairness policy.
 *
 * @param fair {@code true} if this lock should use a fair ordering policy
 */
public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
```

## 3.1 线程抢锁
```java
static final class FairSync extends Sync {
	// 争锁
    final void lock() {
        acquire(1);
    }

    // 来自父类AQS，我直接贴过来这边，下面分析的时候同样会这样做，不会给读者带来阅读压力
    // 我们看到，这个方法，如果tryAcquire(arg) 返回true, 也就结束了。
    // 否则，acquireQueued方法会将线程压到队列中
    public final void acquire(int arg) { // 此时 arg == 1
        // 首先调用tryAcquire(1)一下，名字上就知道，这个只是试一试
        // 因为有可能直接就成功了呢，也就不需要进队列排队了，
        // 对于公平锁的语义就是：本来就没人持有锁，根本没必要进队列等待(又是挂起，又是等待被唤醒的)
        if (!tryAcquire(arg) &&
            // tryAcquire(arg)没有成功，这个时候需要把当前线程挂起，放到阻塞队列中。
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) {
              selfInterrupt();
        }
    }

    /**
     * Fair version of tryAcquire.  Don't grant access unless
     * recursive call or no waiters or is first.
     */
    // 尝试直接获取锁，返回值是boolean，代表是否获取到锁
    // 返回true: 1.没有线程在等待锁；2.重入锁，线程本来就持有锁，也就可以理所当然可以直接获取
    protected final boolean tryAcquire(int acquires) {
    	// 当前线程
        final Thread current = Thread.currentThread();
        int c = getState();
        if (c == 0) {
        	// 如果 state == 0 此时此刻没有线程持有锁
            // 虽然此时此刻锁是可以用的，但是这是公平锁，既然是公平，就得讲究先来后到，
            // 看看有没有别人在队列中等了半天了
            if (!hasQueuedPredecessors() &&
	                // 如果没有线程在等待，那就用CAS尝试一下，成功了就获取到锁了，
	                // 不成功的话，只能说明一个问题，就在刚刚几乎同一时刻有个线程抢先了 =_=
	                // 因为刚刚还没人的，我判断过了
	                compareAndSetState(0, acquires)) {

                // 到这里就是获取到锁了，标记一下，告诉大家，现在是我占用了锁
                setExclusiveOwnerThread(current);
                return true;
            }
        } else if (current == getExclusiveOwnerThread()) {
        	// 会进入这个else if分支，说明是重入了，需要操作：state=state+1
            int nextc = c + acquires;
            if (nextc < 0)
                throw new Error("Maximum lock count exceeded");
            setState(nextc);
            return true;
        }
        // 如果到这里，说明前面的if和else if都没有返回true，说明没有获取到锁
        // 回到上面一个外层调用方法继续看:
        // if (!tryAcquire(arg) 
        //        && acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) 
        //     selfInterrupt();
        return false;
    }


	public final boolean hasQueuedPredecessors() {
	    // The correctness of this depends on head being initialized
	    // before tail and on head.next being accurate if the current
	    // thread is first in queue.
	    Node t = tail; // Read fields in reverse initialization order
	    Node h = head;
	    Node s;
	    // 如果头尾是同一个节点, 返回 false
	    // 如果头尾不同, 并且头节点的下一个节点非空, 或下个节点不是当前线程, 返回 true
	    return h != t &&
	        ((s = h.next) == null || s.thread != Thread.currentThread());
	}
	
	
	// 假设tryAcquire(arg) 返回false，那么代码将执行：
	// acquireQueued(addWaiter(Node.EXCLUSIVE), arg)，
	// 这个方法，首先需要执行：addWaiter(Node.EXCLUSIVE)
	/**
	 * Creates and enqueues node for current thread and given mode.
	 *
	 * @param mode Node.EXCLUSIVE for exclusive, Node.SHARED for shared
	 * @return the new node
	 */
	// 此方法的作用是把线程包装成node，同时进入到队列中
	// 参数mode此时是Node.EXCLUSIVE，代表独占模式
	private Node addWaiter(Node mode) {
	    Node node = new Node(Thread.currentThread(), mode);
	    // Try the fast path of enq; backup to full enq on failure
	    // 以下几行代码想把当前node加到链表的最后面去，也就是进到阻塞队列的最后
	    Node pred = tail;
	
	    // tail!=null => 队列不为空(tail==head的时候，其实队列是空的，不过不管这个吧)
	    if (pred != null) { 
	        // 设置自己的前驱 为当前的队尾节点
	        node.prev = pred; 
	        // 用CAS把自己设置为队尾, 如果成功后，tail == node了
	        if (compareAndSetTail(pred, node)) { 
	            // 进到这里说明设置成功，当前node==tail, 将自己与之前的队尾相连，
	            // 上面已经有 node.prev = pred
	            // 加上下面这句，也就实现了和之前的尾节点双向连接了
	            pred.next = node;
	            // 线程入队了，可以返回了
	            return node;
	        }
	    }
	    // 仔细看看上面的代码，如果会到这里，
	    // 说明 pred==null(队列是空的) 或者 CAS失败(有线程在竞争入队)
	    enq(node);
	    return node;
	}
	
	/**
	 * Inserts node into queue, initializing if necessary. See picture above.
	 * @param node the node to insert
	 * @return node's predecessor
	 */
	// 采用自旋的方式入队
	// 之前说过，到这个方法只有两种可能：等待队列为空，或者有线程竞争入队，
	// 自旋在这边的语义是：CAS设置tail过程中，竞争一次竞争不到，我就多次竞争，总会排到的
	private Node enq(final Node node) {
	    for (;;) {
	        Node t = tail;
	        // 之前说过，队列为空也会进来这里
	        if (t == null) { // Must initialize
	            // 初始化head节点
	            // 细心的读者会知道原来head和tail初始化的时候都是null，反正我不细心
	            // 还是一步CAS，你懂的，现在可能是很多线程同时进来呢
	            if (compareAndSetHead(new Node()))
	                // 给后面用：这个时候head节点的waitStatus==0, 看new Node()构造方法就知道了
	
	                // 这个时候有了head，但是tail还是null，设置一下，
	                // 把tail指向head，放心，马上就有线程要来了，到时候tail就要被抢了
	                // 注意：这里只是设置了tail=head，这里可没return哦，没有return，没有return
	                // 所以，设置完了以后，继续for循环，下次就到下面的else分支了
	                tail = head;
	        } else {
	            // 下面几行，和上一个方法 addWaiter 是一样的，
	            // 只是这个套在无限循环里，反正就是将当前线程排到队尾，有线程竞争的话排不上重复排
	            node.prev = t;
	            if (compareAndSetTail(t, node)) {
	                t.next = node;
	                return t;
	            }
	        }
	    }
	}
	
	// 现在，又回到这段代码了
	// if (!tryAcquire(arg) 
	//        && acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) 
	//     selfInterrupt();
	
	// 下面这个方法，参数node，经过addWaiter(Node.EXCLUSIVE)，此时已经进入阻塞队列
	// 注意一下：如果acquireQueued(addWaiter(Node.EXCLUSIVE), arg))返回true的话，
	// 意味着上面这段代码将进入selfInterrupt()，所以正常情况下，下面应该返回false
	// 这个方法非常重要，应该说真正的线程挂起，然后被唤醒后去获取锁，都在这个方法里了
	final boolean acquireQueued(final Node node, int arg) {
	    boolean failed = true;
	    try {
	        boolean interrupted = false;
	        for (;;) {
	            final Node p = node.predecessor();
	            // p == head 说明当前节点虽然进到了阻塞队列，但是是阻塞队列的第一个，因为它的前驱是head
	            // 注意，阻塞队列不包含head节点，head一般指的是占有锁的线程，head后面的才称为阻塞队列
	            // 所以当前节点可以去试抢一下锁
	            // 这里我们说一下，为什么可以去试试：
	            // 首先，它是队头，这个是第一个条件，其次，当前的head有可能是刚刚初始化的node，
	            // enq(node) 方法里面有提到，head是延时初始化的，而且new Node()的时候没有设置任何线程
	            // 也就是说，当前的head不属于任何一个线程，所以作为队头，可以去试一试，
	            // tryAcquire已经分析过了, 忘记了请往前看一下，就是简单用CAS试操作一下state
	            if (p == head && tryAcquire(arg)) {
	                setHead(node);
	                p.next = null; // help GC
	                failed = false;
	                return interrupted;
	            }
	            // 到这里，说明上面的if分支没有成功，要么当前node本来就不是队头，
	            // 要么就是tryAcquire(arg)没有抢赢别人，继续往下看
	            if (shouldParkAfterFailedAcquire(p, node) &&
	                parkAndCheckInterrupt())
	                interrupted = true;
	        }
	    } finally {
	        if (failed)
	            cancelAcquire(node);
	    }
	}

	/**
     * Checks and updates status for a node that failed to acquire.
     * Returns true if thread should block. This is the main signal
     * control in all acquire loops.  Requires that pred == node.prev
     *
     * @param pred node's predecessor holding status
     * @param node the node
     * @return {@code true} if thread should block
     */
    // 刚刚说过，会到这里就是没有抢到锁呗，这个方法说的是："当前线程没有抢到锁，是否需要挂起当前线程？"
    // 第一个参数是前驱节点，第二个参数才是代表当前线程的节点
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        // 前驱节点的 waitStatus == -1 ，说明前驱节点状态正常，当前线程需要挂起，直接可以返回true
        if (ws == Node.SIGNAL)
            /*
             * This node has already set status asking a release
             * to signal it, so it can safely park.
             */
            return true;

        // 前驱节点 waitStatus大于0 ，之前说过，大于0 说明前驱节点取消了排队。这里需要知道这点：
        // 进入阻塞队列排队的线程会被挂起，而唤醒的操作是由前驱节点完成的。
        // 所以下面这块代码说的是将当前节点的prev指向waitStatus<=0的节点，
        // 简单说，就是为了找个好爹，因为你还得依赖它来唤醒呢，如果前驱节点取消了排队，
        // 找前驱节点的前驱节点做爹，往前循环总能找到一个好爹的
        if (ws > 0) {
            /*
             * Predecessor was cancelled. Skip over predecessors and
             * indicate retry.
             */
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            /*
             * waitStatus must be 0 or PROPAGATE.  Indicate that we
             * need a signal, but don't park yet.  Caller will need to
             * retry to make sure it cannot acquire before parking.
             */
            // 仔细想想，如果进入到这个分支意味着什么
            // 前驱节点的waitStatus不等于-1和1，那也就是只可能是0，-2，-3
            // 在我们前面的源码中，都没有看到有设置waitStatus的，所以每个新的node入队时，waitStatu都是0
            // 用CAS将前驱节点的waitStatus设置为Node.SIGNAL(也就是-1)
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }
}
```

整体流程总结如下:

![](http://oetw0yrii.bkt.clouddn.com/18-7-17/86090722.jpg)