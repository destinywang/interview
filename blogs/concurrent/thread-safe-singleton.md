# 1. 饿汉模式
```java
public class Singleton {
    // 私有构造
    private Singleton() {
        
    }
    
    // 单例对象
    private static Singleton instance = new Singleton();
    
    // 静态工厂方法
    public static Singleton getInstance() {
        return instance;
    }
}
```

优点:
1. 线程安全
2. 实现简单

缺点:
1. 类加载时就需要完成所有初始化工作, 如果初始化工作比较耗时(如涉及到资源的创建), 就会导致性能问题


# 2. 懒汉模式

使用 `double-check` 方式

```java
public class Singleton {
    // 私有构造
    private Singleton() {
        
    }
    
    // 单例对象
    private static Singleton instance = null;
    
    // 静态工厂方法
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

但这种方式依然不能保证严格意义上的线程安全.

> 从 CPU 的角度来看, 在执行 `instance = new Singleton();` 语句时, 会完成以下三步操作:
1. 分配对象内存空间
2. 初始化对象
3. 设置 instance 指向刚分配的内存

在多线程情况下, 由于可能存在 JVM 和 CPU 指令优化而产生的指令重排, 有可能会让 2 和 3 两个步骤的执行顺序发生互换


线程A | 线程B
:-:|:-:
外层 `if (instance == null)` | -
- | `instance = new Singleton()` <br/> 由于指令重排, 目前完成了第三步, 但还未完成第二步<br/> 代表对象此时分配了空间但没有完成初始化
发现 instance 不为空, 于是返回该对象的引用 | -

#### 解决指令重排带来的问题:

将 instance 对象设置为 `volatile`

通过 `volatile` 插入内存屏障的原理, 显式地告诉 CPU 不要进行指令重排.

# 3. 使用枚举来实现

原理: 枚举类的构造方法具有一个特性, 只会被调用一次

```java
public class Singleton {
    // 私有构造
    private Singleton() {
        
    }
    
    // 单例对象
    private static Singleton instance = null;
    
    // 静态工厂方法
    public static Singleton getInstance() {
        return SingletionEnum.INSTANCE.getInstance();
    }
    
    private enum SingletonEnum {
        INSTANCE;
        
        private Singleton singleton;
        
        // JVM 保证只会调用一次
        SingletonEnum () {
            singleton = new Singleton();
        }
        
        public Singleton getInstance() {
            return singleton;
        }
    }
}
```