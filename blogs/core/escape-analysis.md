# 1. 逃逸概念的引入

> 我们都知道, Java 创建的对象都是被分配到堆内存上, 但是事实并不是这么绝对, 通过对Java对象分配的过程分析, 可以知道有两个地方会导致 Java 中创建出来的对象并不一定分别在所认为的堆上. 这两个点分别是 Java 中的 `逃逸分析` 和 `TLAB(Thread Local Allocation Buffer)`线程私有的缓存区。

# 2. 逃逸分析基本概念
逃逸分析, 是一种可以有效减少 Java 程序中同步负载和内存堆分配压力的跨函数全局数据流分析算法. 通过逃逸分析, `Hotspot编译器` 能够分析出一个新的对象的引用的使用范围从而决定是否要将这个对象分配到堆上.

在计算机语言编译器优化原理中, 逃逸分析是指分析指针动态范围的方法, 它同编译器优化原理的指针分析和外形分析相关联. 当变量(或者对象)在方法中分配后, 其指针有可能被返回或者被全局引用, 这样就会被其他过程或者线程所引用, 这种现象称作指针(或者引用)的 `逃逸(Escape)` . 通俗点讲, 如果一个对象的指针被多个方法或者线程引用时, 那么我们就称这个对象的指针发生了逃逸.

## 2.1. 具体分析

逃逸分析研究对于 java 编译器有什么好处呢? 我们知道 java 对象总是在堆中被分配的, 因此 java 对象的创建和回收对系统的开销是很大的. java 语言被批评的一个地方, 也是认为 java 性能慢的一个原因就是  java 不支持栈上分配对象, JDK6里的 `Swing` 内存和性能消耗的瓶颈就是由于 GC 来遍历引用树并回收内存的, 如果对象的数目比较多, 将给 GC 带来较大的压力, 也间接得影响了性能. 减少临时对象在堆内分配的数量, 无疑是最有效的优化方法. java 中应用里普遍存在一种场景, 一般是在方法体内, 声明了一个局部变量, 并且该变量在方法执行生命周期内未发生逃逸, 按照  JVM 内存分配机制, 首先会在堆内存上创建类的实例(对象), 然后将此对象的引用压入调用栈, 继续执行, 这是 JVM 优化前的方式. 当然, 我们可以采用逃逸分析对 JVM  进行优化, 即针对栈的重新分配方式, 首先我们需要分析并且找到未逃逸的变量, 将该变量类的实例化内存直接在栈里分配, 无需进入堆, 分配完成之后, 继续调用栈内执行, 最后线程执行结束, 栈空间被回收, 局部变量对象也被回收, 通过这种方式的优化, 与优化前的方案主要区别在于对象的存储介质, 优化前是在堆中, 而优化后的是在栈中, 从而减少了堆中临时对象的分配(较耗时), 最终完成性能的优化.

> 逃逸分析实际上是 JVM 的一种为优化提供支持的分析手段, 逃逸分析的范围分为两个, 方法逃逸和线程逃逸

### 2.2.1 方法逃逸
> 不逃逸出当前方法, 就是说在一个方法内 `new` 出来的对象, 它的引用没有泄露到这个方法之外
```java
public class Foo {
    int a;
    int b;
    
    public Foo() {
    }
}

public int bar(int a, int b) {
    Foo foo = new Foo();
    foo.a = a;
    foo.b = b;
    // foo 对象没有逃逸出 bar 方法, 只在 bar 方法里当做局部变量存在
    return foo.a + foo.b;
}
```

在上面的例子中, Foo 对象就没有逃逸出 bar 方法, 只有一个局部 foo 变量引用这个对象, foo 变量既没有被当做返回值, 也没有当做另一个方法的参数.

但其实我们一般写的普通 Java Bean 都会有 getter /setter 

```java
public class Foo {
    int a;
    int b;
    
    public Foo(int a, int b) {
        this.a = a;
        this.b = b;
    }
    
    public int getA() {
        return a;
    }
    
    public int getB() {
        return b;
    }
}
```

如果在 `bar()` 方法里依然给 Foo 实例对象赋值, 那肯定就会调用到 Foo 的成员方法 `setA(), setB()`, 把局部变量 foo 的 this 作为参数传递给 foo 的成员方法, 这个时候变量 foo 确实逃逸除了 bar 方法, 而 JIT 提供了 `方法内联`, 在完成方法内联后, 这个参数传递实际上优化掉了.

### 2.2.2 线程逃逸
> 不逃逸出当前线程, 指的是实例对象没有被别的类引用到. 该对象的引用赋值到其他对象的字段, 或其他类的静态字段上, 没办法让它进入一个全局可见的范围, 这个时候我们认为该实例没有逃逸出当前线程

```java
public int bar(int a, int b) {
    Foo foo = new Foo();
    foo.a = a;
    foo.b = b;
    return doBar(foo);
}

public int doBar(Foo foo) {
    return foo.a + foo.b;
}
```

`bar()` 方法调用了 `doBar()`, 把 foo 实例作为入参传入了 `doBar()`, 这个时候认为 foo 逃逸除了 bar 方法, 但是 bar 和 doBar 都在一个类中, 并没有被其他类引用, 我们认为 foo 对象没有逃逸出线程.

## 2.3. JVM 为逃逸分析所做的优化
### 2.3.1 标量替换
> Java 中标量的意思是不能再分割的量, 如基本类型和 Reference 类型, 反之成为聚合量, 如果把一个对象拆开, 将它的成员变量分割成标量, 这个就叫标量替换.

如果逃逸分析发现一个对象不会被外部访问, 并且该对象可以被拆散, 那么经过优化后, 并不直接生成该对象, 而是在栈上创建若干个成员变量, 原本的对象就无需再堆上整体分配空间了.

    栈帧内分配对象的行为成为栈上分配, 目的是减少新生代的 GC 频率, 见解提高 JVM 性能, 通过 -XX+EliminateAllcations 可以开启标量替换.
    
### 2.3.2 锁消除优化
> Java 方法中返回值如果没有被其他类用到, 那这个对象就不会逃逸出线程, 我们知道变量的读写竞争的时候需要加锁访问, 如果确定该变量不会逃逸出该线程, 那同步访问控制就可以优化掉.

## 2.4 实操

```java
public class User {
    private Sting name;
    private int age;
    
    // getters / setters / constructors
}

public int bar(Foo foo) {
    User user = new User(23);
    return foo.getA() + foo.getB() + user.getAge();
}

public static void main(String[] args) {
    Foo foo = new Foo();
    for(int i = 0; i < 1000000000; ++i) {
        foo.setA(4);
        foo.setB(45);
    }
}
```
### 2.4.1 关闭逃逸分析
启动 JVM 参数

    -server
    -XX:-DoEscapeAnalysis

使用 `jmap -histo`
![](http://oetw0yrii.bkt.clouddn.com/18-8-30/3446787.jpg)

### 2.4.2 打开逃逸分析
JVM 参数

    -server
    -XX:+DoEscapeAnalysis
    
![](http://oetw0yrii.bkt.clouddn.com/18-8-30/67522193.jpg)

可以看到, 只有少量的对象在堆上实例化, 大部分对象的属性被标量替换了.

# 3. JIT 编译
在 JVM 中触发 JIT 编译是基于两个计数器:
1. 一个方法被调用的次数
2. 存在有分支的方法中的循环次数, 如果方法里面有一个很长的循环, 这时候需要编译到这个循环, 每一次分支的循环被调用, 该分支的计数器都会增加

增加 `-XX:+PrintCompileation` 参数观察 JVM 输出的编译日志

    -
     96    1       3       java.lang.String::equals (81 bytes)
     96    4       3       java.io.UnixFileSystem::normalize (75 bytes)
     97    9       3       java.lang.String::hashCode (55 bytes)
     97    8       3       java.lang.Object::<init> (1 bytes)
     97    7       3       java.lang.AbstractStringBuilder::ensureCapacityInternal (16 bytes)
     98    3       3       java.lang.String::length (6 bytes)
     98   10       3       java.lang.Math::min (11 bytes)
     98    2       3       java.lang.System::getSecurityManager (4 bytes)
     98    6       3       java.util.Arrays::copyOf (19 bytes)
     98   11     n 0       java.lang.System::arraycopy (native)   (static)
     98   12       3       java.lang.String::indexOf (70 bytes)
     98   15       3       sun.nio.cs.UTF_8$Encoder::encode (359 bytes)
     99   13       4       java.lang.String::charAt (29 bytes)
     99   16       3       java.lang.String::lastIndexOf (52 bytes)
     99    5       3       java.util.HashMap::hash (20 bytes)
    100   18       3       java.lang.String::<init> (82 bytes)
    100   14       3       java.lang.StringBuilder::toString (17 bytes)
    100   19       3       java.lang.String::startsWith (72 bytes)
    100   20       1       java.util.ArrayList::size (5 bytes)
    100   17       1       java.lang.ref.Reference::get (5 bytes)
    101   21       1       sun.instrument.TransformerManager::getSnapshotTransformerList (5 bytes)
    101   22       3       java.lang.String::startsWith (7 bytes)
    101   23       3       java.lang.String::indexOf (166 bytes)
    102   26       1       java.lang.Object::<init> (1 bytes)
    102    8       3       java.lang.Object::<init> (1 bytes)   made not entrant
    102   30       3       org.destiny.demo.Foo::setA (6 bytes)
    102   31       3       org.destiny.demo.Foo::setB (6 bytes)
    102   32       3       org.destiny.demo.User::bar (25 bytes)
    102   33       3       org.destiny.demo.User::<init> (10 bytes)
    103   34       1       org.destiny.demo.Foo::setA (6 bytes)
    103   30       3       org.destiny.demo.Foo::setA (6 bytes)   made not entrant
    103   35       1       org.destiny.demo.Foo::setB (6 bytes)
    103   31       3       org.destiny.demo.Foo::setB (6 bytes)   made not entrant
    103   27       1       org.destiny.demo.Foo::getA (5 bytes)
    103   28       1       org.destiny.demo.Foo::getB (5 bytes)
    103   29       1       org.destiny.demo.User::getAge (5 bytes)
    103   24       3       java.lang.String::endsWith (17 bytes)
    103   36       4       org.destiny.demo.User::bar (25 bytes)
    103   25       3       java.lang.ref.SoftReference::get (29 bytes)
    104   32       3       org.destiny.demo.User::bar (25 bytes)   made not entrant
    104   37       1       java.lang.ThreadLocal::access$400 (5 bytes)
    106   38       3       java.lang.String::indexOf (7 bytes)
    106   39       3       java.lang.Character::toLowerCase (9 bytes)
    106   40       3       java.lang.CharacterDataLatin1::toLowerCase (39 bytes)
    108   41 %     3       org.destiny.demo.User::main @ 18 (48 bytes)
    108   42       3       org.destiny.demo.User::main (48 bytes)
    109   43 %     4       org.destiny.demo.User::main @ 18 (48 bytes)
    112   41 %     3       org.destiny.demo.User::main @ -2 (48 bytes)   made not entrant
    150   43 %     4       org.destiny.demo.User::main @ -2 (48 bytes)   made not entrant 
    
编译日志分为 7 列, 依次是
- 时间(基于 JVM 启动的时间戳)
- 编译任务 id(基本递增)
- 编译属性
- tiered_level(分为 4 级)
- 方法信息
- 占用字节数
- deopt

其中, 编译属性 `attribute` 分为:

属性值 | 属性描述
---|---
% | The compilation is OSR
s | The method is synchronized
! | The method has an exception handler
b | Compliation occurred in blocking mode
n | Compliation occurred for a wrapper to a native method

tiered_level:

值 | 描述
---|---
0 | Interpreted Code
1 | Simple C1 Compiled Code
2 | Limited C1 Compiled Code
3 | Full C1 Compiled Code
4 | C2 Compile Code