# 1. 背景知识
## 1.1 文件描述符(fd)

> 文件描述符(File Descriptor)是计算机科学中的一个术语, 是一个用于表述指向文件的引用的抽象化概念.

文件描述符在形式上是一个非负整数, 实际上, 它是一个索引值, 指向内核为每一个进程所维护的该进程打开文件的记录表.  
当程序打开一个现有文件或者创建一个新文件时, 内核向进程返回一个文件描述符. 在程序设计中, 一些涉及底层的程序编写往往会围绕着文件描述符展开. 但是文件描述符这一概念往往只适用于 UNIX / Linux 这样的操作系统. 在 Linux 系统中, 流在内核中可以表示成文件的形式.

## 1.2 IO 模型

> IO 可以理解为对流的操作.

一般对于一个 `read` 操作发生时, 它会经历两个阶段
1. 等待数据准备;
2. 真正的读取过程, 将数据从内核缓冲区拷贝到用户进程缓冲区中.

常见的五种 IO 模型也是围绕这两个阶段来区分的:
- 同步模型
    - 阻塞 IO
    - 非阻塞 IO
    - 多路复用 IO
    - 信号驱动式 IO
- 异步 IO

其中, IO 多路复用就是一种机制, 实现一个进程可以监视多个描述符, 一旦某个描述符就绪, 就能够通知程序进行相应的读写操作. IO多路复用相比于多线程的优势在于系统的开销小, 系统不必创建和维护进程或线程, 免去了线程或进程的切换带来的开销.

而操作系统支持 IO 多路复用的系统调用有 `select`, `poll` 和 `epoll`.

# 2. select
select 的声明:

```
int select(int n, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, struct timeval *timeout);
```

`fd_set` 是表示文件描述符集合的数据结构, `readfds`, `writefds` 和 `exceptfds` 分别对应三类文件描述符集, 当 select 被调用时, 内部逻辑如下: 
1. 将 3 个 fd 集 copy 到内核, 这里限制了 fd 最大数量为 1024;
2. 线程阻塞, 直到超时或内核检测到有 fd 可读或可写, 内核会通知监控者select, select 返回可读或可写的 fd 总数;
3. 用户进程通过遍历的方式找到可读写的 fd, 时间复杂度为 o(n), IO效率随着fd数量增多而线性下降

缺点:
1. copy 次数过多, 而且每次调用 select 方法都要进行 fd 集的 copy 操作;
2. select 监控 fd 数量有限;
3. 用户进程通过遍历的方式找到可读写的 fd, 时间复杂度为o(n), IO效率随着fd数量增多而线性下降.

# 3. poll
poll 的声明:

```
int poll(struct pollfd *fds, unsigned int nfds, int timeout);
```

`pollfd` 是表示文件描述符集合的数据结构:

```
struct pollfd {
    int fd; //文件描述符
    short events; //监视的请求事件
    short revents; //已发生的事件
};
```

poll 与 select 差不多, 但 poll 的 pollfd 没有最大数量的限制, 可是 IO 效率依旧没有提升.

# 4. epoll

epoll 的操作过程有三个方法:
- epoll_create();
- epoll_ctl();
- epoll_wait();

## 4.1 epoll_create

```
int epoll_create(int size);     
```
> 用于创建一个 epoll 的句柄, size是指监听的描述符个数.

该方法会在内核创建专属于 epoll 的高速 cache 区, 并在该缓冲区建立红黑树和就绪链表, 用户态传入的文件句柄将被放到红黑树中.

## 4.2 epoll_ctl

```
int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event);
```
> 该方法对 epoll_create() 所创建的内核 cache 区进行操作, 操作对象是需要监听的 fd.

比如, 把要监听的 fd 注册到 cache 内, 那么epoll_ctl() 会将 fd 插入到红黑树中, 并向内核注册了该 fd 的回调函数.  
 
内核在检测到某 fd 可读可写时则调用该回调函数, 而回调函数的工作是将 fd 放到就绪链表.

## 4.3 epoll_wait

epoll 是 Linux 下多路复用 IO 接口 `select/poll` 的增强版本.

它能显著减少程序在大量并发连接中只有少量活跃的情况下的系统 CPU 利用率, 因为它不会复用文件描述符集合来传递结果而迫使开发者每次等待事件之前都必须重新准备要被侦听的文件描述符集合.

另一点原因就是获取事件的时候, 它无须遍历整个被侦听的描述符集, 只要遍历那些被内核IO事件异步唤醒而加入Ready队列的描述符集合就行了.  


```
int epoll_wait(int epfd, struct epoll_event *events,int maxevents, int timeout);
```

> `epoll_wait` 只需监控就绪链表, 如果就绪链表有 fd, 则表示该 fd 可读可写, 并返回给用户态(少量的 copy)

该函数返回需要处理的事件数目, 如返回0表示已超时

- 执行 `epoll_create` 时, 创建了红黑树和就绪链表; 
- 执行 `epoll_ctl` 时, 如果增加fd, 则检查在红黑树中是否存在, 存在立即返回, 不存在则添加到树上, 然后向内核注册回调函数, 用于当中断事件到来时向准备就绪链表中插入数据. 
- 执行 `epoll_wait` 时, 返回就绪链表里的数据即可。



因此, `epoll` 比 `select` 和 `poll` 高效的原因是:
1. 减少了用户态和内核态之间文件句柄的 copy;
2. 降低了在文件句柄集中查找的时间复杂度, 用红黑树维护 fd 集, 可以将查找fd的时间复杂度降为 o(logn).