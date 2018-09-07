# 1. 同步与异步
这两个概念与消息的通知机制有关.

> 一个用户去银行办理业务, 有两个选择: 亲自去排队, 和叫人代办;  
对他来说, 自己去办理是同步方式, 叫别人代办是异步方式.

二者的区别在于:
- 同步方式下操作者主动完成了这件事情
- 而在异步方式下, 调用指令发出后, 操作马上就返回了, 操作者并不能马上知道结果, 而是等待所调用的异步过程处理完毕后, 再通过通知的手段(在代码中通常是回调函数)来告诉操作者结果.


![](http://oetw0yrii.bkt.clouddn.com/18-9-7/28907986.jpg)

在上面这张异步 IO 模型中, 应用程序调用完 `aio_read` 之后, 不论是否有数据可读, 这个操作都会马上返回, 整个过程相当于这个例子中委托另一个人去帮忙代办银行业务的过程, 当数据读完拷贝到用户内存之后, 会发一个信号通知原进程告诉读数据操作已经完成(而不仅仅是有数据可读).

# 2. 阻塞与非阻塞
这两个概念与程序处理事务时的状态有关.

## 2.1 阻塞

> 当真正执行办理业务的人去银行办理时, 前面可能已经有人排队等候, 如果这个人从排队到办理完毕, 中间一直都没有做过其他事情, 那么这个过程就是阻塞的, 因为这个人当前只在做这么一件事.

![](http://oetw0yrii.bkt.clouddn.com/18-9-7/29478316.jpg)

在上图中, 应用程序发出 recvfrom 操作后, 要等待数据拷贝成功才能返回, 整个过程中, 不能做其他操作.

## 2.2 非阻塞

> 反之, 如果这个人发现前面排队的人比较多, 于是决定领个号码, 然后逛一逛, 过段时间再回来看看有没有轮到他的号被叫到, 如果没有又继续出去逛, 重复此步骤直到自己的号被叫到, 这个过程就是非阻塞, 因为处理这个事情的人在整个过程中并没有做到除了这件事以外不做别的事情, 他的做法是反复地过来检测, 如果没有完成就下一次再尝试完成这件事情.

![](http://oetw0yrii.bkt.clouddn.com/18-9-7/6757863.jpg)

上图与阻塞式 IO 最大的区别在于: 当没有数据可读时, 同样的 `recvfrom` 操作返回了错误码, 表示当前没有可读数据. 换言之, 及时没有数据也不会一直让这个应用阻塞在这个调用上, 这就是非阻塞 IO.

- 阻塞与非阻塞: 区别在于完成一件事时, 当事情还没有完成, 处理这件事的人除此之外能否做其他事情;
- 同步与异步: 区别在于是你自己去做这件事, 还是等别人做好了来通知有结果, 然后再自己去拿结果.

由此可见, 两组概念不在同一个纬度.

# 3. 多路复用 IO
多路复用 IO 记录下来有哪些人在等待消息的通知, 在条件满足时负责通知办理者, 而完成这件事还是需要自己去完成, 只要是自己去完成的操作, 都是同步操作

出自 UNIX 网络编程
> POSIX defines these two terms as follows:  
A synchronous I/O operation cause the requesting process to be blocked until that I/O operation completes.  
An asynchronous I/O operation dose not cause the requesting process to be blocked.  
Using these defintions, the first fout I/O models - blocking, nonblocking, I/O multiplexing, and signal-driven I/O - are all synchronous because the actual I/O operation(recvfrom) blocks the process. Only the asynchronous I/O model matches the asynchronous I/O definition.

> POSIX对这两个术语的定义如下:  
同步I/O操作会导致请求进程阻塞，直到I/O操作完成。  
异步I/O操作不会导致请求进程被阻塞。  
使用这些定义，前四个I/O模型——阻塞、非阻塞、I/O多路复用和信号驱动I/O——都是同步的，因为实际的I/O操作(recvfrom)阻塞了进程。只有异步I/O模型匹配异步I/O定义。