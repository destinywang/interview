# 1. 概念

    AtomicInteger 是对 Integer 的一个封装, 提供原子性的读写操作, 其原子性是基于 CAS 的一种技术.
    
CAS 是一系列操作的集合, 其底层是一个 CPU 指令, 这些操作具有如下特点:
> 获取当前数值, 进行一系列操作, 利用 CAS 指令试图进行更新, 如果当前数值未变, 代表没有其他线程进行并发修改, 则成功更新;  
否则, 要么进行重试, 要么返回一个成功或失败的结果.

```java
// setup to use Unsafe.compareAndSwapInt for updates
private static final Unsafe unsafe = Unsafe.getUnsafe();
private static final long valueOffset;

static {
    try {
        valueOffset = unsafe.objectFieldOffset
            (AtomicInteger.class.getDeclaredField("value"));
    } catch (Exception ex) { 
        throw new Error(ex); 
    }
}

private volatile int value;
```

从内部属性可以看出, 它依赖于 `Unsafe` 提供的一些底层能力.

在 `getAndIncrement()` 方法中
`Unsafe` 会利用 value 字段的内存地址偏移, 直接完成操作.
```java
/**
 * Atomically increments by one the current value.
 *
 * @return the previous value
 */
public final int getAndIncrement() {
    return unsafe.getAndAddInt(this, valueOffset, 1);
}
```

由于 `getAndIncrement` 需要返回数值, 因此需要添加失败重试逻辑
```java
public final int getAndAddInt(Object o, long offset, int delta) {
    int v;
    do {
        v = this.getIntVolatile(o, offset);
    } while(!this.compareAndSwapInt(o, offset, v, v + delta));

    return v;
    }
```

# 2. CAS 的副作用
## 2.1 资源竞争情况

    CAS 常用的失败竞争机制, 在竞争程度不高的情况下是非常有效的;
    但在竞争激烈的情况下, 就会对 CPU 造成较大的消耗.
    
## 2.2 ABA 问题

    由于 CAS 是在竞争时比较前值, 如果对方只是恰好相等, 仅仅判断数值是 A, 可能导致不合理的修改操作.
    
针对这种情况, Java 提供了 `AtomicStampedReference` 工具类, 用过为引用创建类似版本号的方式, 来保证 CAS 的正确性.
