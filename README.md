# 常见面试题汇总

主要来源:
- 网上看到的面经, 在此自己尝试作答
- 日常开发和学习中遇到的问题, 自问自答
- 身边同学的亲身经历

统一收集在此, 作为自学的一种方式.

| [Java 核心概念](#core) | [集合](#集合) | [并发](#并发) | [JVM](#jvm) | [框架](#框架) | [中间件](#中间件) | [数据库](#数据库) | [操作系统](#操作系统) | [网络](#网络) | [数据结构/算法](#算法) | [分布式系统](#分布式系统) | [架构设计](#架构设计) | [面向对象](#面向对象) | [常用工具](#常用工具) |
|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|

## <span id="core">核心概念</span>
- [Class.forName() 和 ClassLoader 的区别](https://github.com/DestinyWang/interview/blob/master/blogs/core/the-difference-between-forName-and-ClassLoader.md)
- [对象逃逸分析](https://github.com/DestinyWang/interview/blob/master/blogs/core/escape-analysis.md)
- [谈谈对强软弱虚四种引用的理解](https://github.com/DestinyWang/interview/blob/master/blogs/core/strong-soft-weak-virtual-reference.md)
- [String, StringBuffer, StringBuilder 的区别](https://github.com/DestinyWang/interview/blob/master/blogs/core/string-stringbuilder-stringbuffer.md)

## <span id="collections">集合</span>

## <span id="concurrent">并发</span>
- [AtomicInteger 底层原理与 CAS](https://github.com/DestinyWang/interview/blob/master/blogs/concurrent/atomic-integer.md)
- [Java 并发包提供的工具](https://github.com/DestinyWang/interview/blob/master/blogs/concurrent/java-concurrent-util.md)
- [线程安全的单例模式](https://github.com/DestinyWang/interview/blob/master/blogs/concurrent/thread-safe-singleton.md)
- [AQS 源码分析](https://github.com/DestinyWang/interview/blob/master/blogs/concurrent/abstract-queue-synchronized.md)

## <span id="jvm">JVM</span>
- [JVM 内存划分以及 OOM 的理解](https://github.com/DestinyWang/interview/blob/master/blogs/jvm/memory-partitioning-of-jvm.md)
- [Java 常见的垃圾收集器](https://github.com/DestinyWang/interview/blob/master/blogs/jvm/garbage-collector.md)
- [CMS GC](https://github.com/DestinyWang/interview/blob/master/blogs/jvm/cms-gc.md)
- [G1 GC](https://github.com/DestinyWang/interview/blob/master/blogs/jvm/g1-gc.md)
- [GC 调优](https://github.com/DestinyWang/interview/blob/master/blogs/jvm/the-gc-tuning.md)
- [谈谈线上 JVM 的启动参数](https://github.com/DestinyWang/interview/blob/master/blogs/jvm/jvm-startup-parameters.md)

## <span id="framework">框架</span>
- [Spring Bean 的生命周期和作用域](https://github.com/DestinyWang/interview/blob/master/blogs/framework/the-life-cycle-and-scope-of-Spring-beans.md)
- [Netty 核心概念](https://github.com/DestinyWang/interview/blob/master/blogs/framework/Netty.md)
- [Netty 如何解决空轮询](https://github.com/DestinyWang/interview/blob/master/blogs/framework/how-netty-solves-the-empty-wheel-training.md)

## <span id="middleware">中间件</span>
- [ZAB 协议的基本概念](https://github.com/DestinyWang/interview/blob/master/blogs/middleware/zab.md)

## <span id="db">数据库</span>
- [InnoDB 如何实现高并发](https://github.com/DestinyWang/interview/blob/master/blogs/db/innodb-transaction-model.md)


## <span id="os">操作系统</span>
- [从打开电源到出现显示桌面, 计算机都干了些什么事情](https://github.com/DestinyWang/interview/blob/master/blogs/os/what-does-the-computer-do-from-turning-on-the-power-to-the-display-desktop.md)
- [Linux 启动时遇到错误应该如何处理](https://github.com/DestinyWang/interview/blob/master/blogs/os/how-to-troubleshoot-a-linux-boot-failure.md)

## <span id="network">网络</span>

## <span id="algorithm">数据结构/算法</span>
- [三角形最小路径和](https://github.com/DestinyWang/interview/blob/master/blogs/algorithm/Triangle.md)
- [找到所有数组中消失的数字](https://github.com/DestinyWang/interview/blob/master/blogs/algorithm/Find-All-Numbers-Disappeared-in-an-Array.md)

## <span id="distributed">分布式系统</span>
- [CAP 理论简介](https://github.com/DestinyWang/interview/blob/master/blogs/distributed/CAP.md)
- [分布式事务](https://github.com/DestinyWang/interview/blob/master/blogs/distributed/distributed-transaction.md)

## <span id="architect">架构设计</span>
- [系统如何在不重启的情况下接收新的请求类型并转换成统一的内部模型](https://github.com/DestinyWang/interview/blob/master/blogs/architect/how-does-the-system-receive-new-request-types-without-a-reboot-and-switch-to-a-unified-internal-model.md)
- [如何实现分布式锁](https://github.com/DestinyWang/interview/blob/master/blogs/architect/distributed-lock.md)

## <span id="OOD">面向对象设计</span>
- [贫血模型, 充血模型, 胀血模型, 领域驱动设计](https://github.com/DestinyWang/interview/blob/master/blogs/ood/domain-driven-design.md)

## <span id="util">常用工具</span>
- [Apache Bench 和 JMeter](https://github.com/DestinyWang/interview/blob/master/blogs/util/apache-bench-and-jmeter.md)
- [单元测试](https://github.com/DestinyWang/interview/blob/master/blogs/util/unit-test.md)