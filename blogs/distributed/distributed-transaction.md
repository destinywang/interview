# 1. 定义

    事务能够提供一种将一个活动涉及到的所有操作纳入到一个不可分割的执行单元的机制.
    组成事务的操作只有在操作均能正常执行的情况下才能提交, 只要其中任意一步执行失败, 都将导致整个操作回滚.
    
    
# 2. 数据库本地事务
## 2.1 ACID

header 1 | header 2
---|---
原子性 | 一个事务的所有操作, 要么全部完成, 要么全部不完成, 不会结束在中间某个环节
一致性 | 在一个事务执行之前和执行之后, 数据库必须保持处于一致的状态. 如果事务成功执行, 系统中所有的变化都将正确地被应用, 反之, 所有变化都将被回滚
隔离性 | 当不同的事务操作相同的数据的时候, 每个事务都有各自的完整数据空间, 由事务所做的修改必须与任何其他事务所做的修改隔离, 事务不会看到数据的中间状态.
持久性 | 只要事务成功结束, 它对数据库所做的更新就必须永久保存下来

![](http://oetw0yrii.bkt.clouddn.com/18-8-4/3451917.jpg)

而事务的 ACID 是通过 InnoDB 日志和锁来保证.

- 事务的隔离性是通过数据库锁的机制来实现;
- 持久性是通过 redo log(重做日志) 来实现的
- 原子性和一致性是通过 undo log(回滚日志)


    Undo log: 为了满足事务的原子性, 在操作任何数据之前, 首先将数据备份到一个地方, 然后对数据进行修改, 如果出现了错误, 或者用户执行 RollBack, 系统可以利用 Undo log 中的备份将数据恢复到事务开始之前的状态
    Redo log: 记录新数据的备份, 在事务提交之前, 只要将 Redo log 持久化即可, 当系统崩溃时, 虽然数据没有持久化, 但是 Redo log 已经持久化, 系统可以根据 Redo log 的内容, 将所有数据恢复到最新状态.
    
# 3. 分布式事务
## 3.1 分布式事务概念

    指事物的参与者, 支持事务的服务器, 资源服务器以及事务管理器分别位于不同的分布式系统之上.
    本质上讲, 分布式事务就是为了保证不同数据库的数据一致性.
    
## 3.2 场景
### 3.2.1 service 多个节点
> 随着互联网快速发展, SOA, 微服务等架构模式正在被大规模使用, 一个公司内, 用户的资产可能被分为好多个部分, 比如余额, 积分, 优惠券等

![](http://oetw0yrii.bkt.clouddn.com/18-8-4/44346101.jpg)

这样的话传统的单机事务实现方式无法保证积分扣减成功之后, 优惠券也能正确完成扣减操作.

### 3.2.2 resource 多个节点

> 同样, 由于单表数据过大需要进行拆分, 一次转账业务需要在北京的 MySQL 实例向 上海的 MySQL 实例转账, 同样无法保证他们能同时成功.

![](http://oetw0yrii.bkt.clouddn.com/18-8-4/47229853.jpg)

## 3.3 分布式事务基础
### 3.3.1 CAP
- C: 对某个执行的客户端来说, 读操作能返回最新的写操作. 对于数据分布在不同节点上的数据来说, 如果在某个节点更新了数据, 那么在其他节点都能读取到最新的数据, 那么就成为强一致, 反之就是分布式不一致;
- A: 非故障的节点在一定时间内返回合理的响应(不是错误或超时), 可用性的关键在于: `合理的时间` 和 `合理的响应`, 请求不能无限期得不到响应, 并且需要得到系统正确的返回结果;
- P: 当出现网络分区后, 系统依然能够正常工作.

在分布式系统中, 网络永远无法 100% 可靠, 分区是一个一定会出现的情况, 如果我们选择 AC 而放弃 P, 当分区发生时, 为了保证一致性, 这个时候必须拒绝请求, 当时 A 又不允许拒绝, 所以分布式系统理论上不可能选择 CA 架构, 只能选择 CP 或者 AP 架构.

对于 CP 来说, 放弃可用性, 追求一致性和分区容错性, 比如 Zookeeper 就是追求强一致.

对于 AP 来说, 放弃一致性(强一致), 追求分区容错和可用, 这是很多分布式系统的选择.

CAP 是忽略网络延迟的, 也就是当事务提交时, 从节点 A 复制到节点 B, 但是在现实中总会有一定的时间延迟.

### 3.3.2 BASE

    基本可用, 软状态, 最终一致性的缩写
    本质上是 AP 的一个扩张, 通过软状态实现基本可用和最终一致性.

- BA: 基本可用, 分布式系统出现故障时, 允许损失部分可用功能, 保证核心功能可用;
- 软状态: 允许系统中存在中间状态, 这个状态不影响系统可用性, 这里指的是 CAP 中的不一致;
- 最终一致性: 经过一段时间后, 所有节点数据都将达到一致.

# 4. 分布式事务的解决方案
## 4.1 是否真的需要分布式事务

    首先要明确是否真的需要分布式事务?
    
是否存在由于服务拆分过细导致不合理的分布式系统设计?

可以先考虑将多个微服务聚合成一个单机服务, 避免引入不必要的成本和复杂度.

## 4.2 2PC

![image](http://images2015.cnblogs.com/blog/524341/201607/524341-20160718200514638-1914892480.png)

第一阶段: 事务管理器要求每个涉及到事务的数据库预提交此操作, 并反映是否可以提交

第二节点: 事务协调器要求每个数据库提交数据, 或者回滚

优点: 保证数据强一致, 实现简单;

缺点: 事务管理器存在单机风险; 整个过程存在同步阻塞; 数据可能不一致; 不支持高并发.

## 4.3 TCC

相比 2PC, 解决了以下问题
1. 解决了协调者单点, 引入集群
2. 引入超时, 超时后进行补偿, 并且不会锁定整个资源, 将资源转换为业务逻辑形式
3. 数据一致性, 有了补偿机制后, 由业务管理其控制一致性


    Try 阶段: 尝试执行, 完成所有业务检查(一致性), 预留必须业务资源(准隔离性)
    
    Confirm 阶段: 确认执行真正的业务, 不做任何业务检查, 只使用 Try 阶段预留的业务资源, Confirm 操作满足幂等性. 要求具备幂等设计, Confirm 失败后需要进行重试.
    
    Cancel 阶段: 取消执行, 释放 Try 阶段预留的业务资源, 也需要满足幂等性.
    
![](http://oetw0yrii.bkt.clouddn.com/18-8-4/81578002.jpg)

## 4.4 本地消息表

    将需要分布式处理的任务通过消息日至的方式来异步执行
    
消息日志可以存储到本地文本, 数据库或者消息队列, 再通过业务规则或人工发起重试, 人工重试更多应用于支付系统

> 举一个购物的例子

1. 当账户扣款的时候, 需要在扣款相关的服务上新增一个本地消息表, 需要把记录扣款和写入扣减商品库存的本地消息表放入同一个事务.
2. 有个定时任务去轮询本地事务表, 把没有发送的消息扔给商品服务, 让它扣减库存, 到达商品服务后, 先写入这个服务器的事务表, 再进行扣减, 扣减成功后, 更新事务表中的状态;
3. 商品服务器通过定时任务扫描消息表或者直接通过扣款服务吗扣款服务本地消息表进行更新;
4. 针对特定情况, 定时扫描未成功处理的消息, 进行重新发送, 在商品服务收到消息后, 先判断是否是重复消息, 如果已经接受, 再判断是否执行, 如果执行再马上又进行通知事务, 如果未执行, 就需要重新执行需要由业务保证幂等.

![](http://oetw0yrii.bkt.clouddn.com/18-8-4/62419454.jpg)

1. 在分布式事务操作的一方完成写业务数据的操作之后向本地消息表发送一个消息，本地事务能保证这个消息一定会被写入本地消息表中。
2. 之后将本地消息表中的消息转发到 Kafka 等消息队列中，如果转发成功则将消息从本地消息表中删除，否则继续重新转发。
3. 在分布式事务操作的另一方从消息队列中读取一个消息，并执行消息中的操作。


## 4.5 MQ 事务
还是以转账的模型举例:
![image](https://upload-images.jianshu.io/upload_images/175724-92abb226f288ff9c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/621)

#### 1. 先发送消息
![image](https://upload-images.jianshu.io/upload_images/175724-1927b8f3d14ef823.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/618)

如果消息发送成功，但是扣款失败，消费端就会消费此消息，进而向Smith账户加钱。

#### 2. 先扣款
![image](https://upload-images.jianshu.io/upload_images/175724-367b5cf60cbdfa16.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/619)

如果扣款成功，发送消息失败，就会出现Bob扣钱了，但是Smith账户未加钱。

#### 3. RocketMQ 的实现
![image](https://upload-images.jianshu.io/upload_images/175724-ab0085543c6d02d6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/621)

1. 发送 `Prepared` 消息时，会拿到消息的地址;
2. 执行本地事物;
3. 通过第一阶段拿到的地址去访问消息, 并修改消息的状态.

这样可以保证消息发送消息和本地事务执行成功保持原子性操作.

#### 问题1: 如果步骤 3 失败怎么办

    RocketMQ会定期扫描消息集群中的事物消息，如果发现了Prepared消息，它会向消息发送端(生产者)确认，Bob的钱到底是减了还是没减呢？
    如果减了是回滚还是继续发送确认消息呢？RocketMQ会根据发送端设置的策略来决定是回滚还是继续发送确认消息。这样就保证了消息发送与本地事务同时成功或同时失败。
    
```java
// =============================发送事务消息的一系列准备工作========================================
// 未决事务，MQ服务器回查客户端
// 也就是上文所说的，当RocketMQ发现`Prepared消息`时，会根据这个Listener实现的策略来决断事务
TransactionCheckListener transactionCheckListener = new TransactionCheckListenerImpl();
// 构造事务消息的生产者
TransactionMQProducer producer = new TransactionMQProducer("groupName");
// 设置事务决断处理类
producer.setTransactionCheckListener(transactionCheckListener);
// 本地事务的处理逻辑，相当于示例中检查Bob账户并扣钱的逻辑
TransactionExecuterImpl tranExecuter = new TransactionExecuterImpl();
producer.start()
// 构造MSG，省略构造参数
Message msg = new Message(......);
// 发送消息
SendResult sendResult = producer.sendMessageInTransaction(msg, tranExecuter, null);
producer.shutdown();
```

接着查看 `sendMessageInTransaction` 方法的源码，总共分为3个阶段：发送Prepared消息、执行本地事务、发送确认消息。

```java
//  ================================事务消息的发送过程=============================================
public TransactionSendResult sendMessageInTransaction(.....)  {
    // 逻辑代码，非实际代码
    // 1.发送消息
    sendResult = this.send(msg);
    // sendResult.getSendStatus() == SEND_OK
    // 2.如果消息发送成功，处理与消息关联的本地事务单元
    LocalTransactionState localTransactionState = tranExecuter.executeLocalTransactionBranch(msg, arg);
    // 3.结束事务
    this.endTransaction(sendResult, localTransactionState, localException);
}
```

`endTransaction` 方法会将请求发往 `broker(mq server)` 去更新事务消息的最终状态：

1. 根据sendResult找到Prepared消息 ，sendResult包含事务消息的ID
2. 根据localTransaction更新消息的最终状态


#### 问题2: Consumer 消费失败怎么办

    如果Bob的账户的余额已经减少，且消息已经发送成功，Smith端开始消费这条消息，这个时候就会出现消费失败和消费超时两个问题.
    解决超时问题的思路就是一直重试，直到消费端消费消息成功，整个过程中有可能会出现消息重复的问题，按照前面的思路解决即可。
    
![image](https://upload-images.jianshu.io/upload_images/175724-1d9ba7bcd230e0dc.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/624)