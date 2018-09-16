# kill 命令的实现原理
## 1. 作用

    用来终止一个进程
    
## 2. 格式

    kill [ －s signal | －p ] [ －a ] pid ... 
    kill －l [ signal ] 
    
## 3. 参数
- -s: 指定发送的信号
- -p: 模拟发送的信号
- -l: 指定信号的名称列表
- pid: 要终止进程的 ID 号
- signal: 信号

## 4. 说明
进程是 Linux 系统中一个非常重要的概念, Linux 是一个多任务的操作系统, 系统上经常同时运行着多个进程. 我们不关心这些进程究竟是如何分配的, 或者是内核如何管理分配时间片的, 所关心的是如何去控制这些进程, 让它们能够很好地为用户服务.

Linux 操作系统包括三种不同类型的进程, 每种进程都有自己的特点和属性:
- 交互进程是由一个 Shell 启动的进程, 交互进程既可以在前台运行, 也可以在后台运行. 
- 批处理进程和终端没有联系, 是一个进程序列. 
- 监控进程(也称系统守护进程) 是 Linux 系统启动时启动的进程, 并在后台运行. 例如, `httpd` 是著名的 Apache 服务器的监控进程.

> kill 命令的工作原理是: 向 Linux 系统的内核发送一个系统操作信号和某个程序的进程标识号, 然后系统内核就可以对进程标识号指定的进程进行操作. 

比如在 `top` 命令中, 我们看到系统运行许多进程, 有时就需要使用 kill 中止某些进程来提高系统资源.

## 5. 实例
### 5.1 强行中止(经常使用杀掉)一个进程标识号为324的进程: 
```bash
kill -9 324
```

### 5.2 解除Linux系统的死锁
在 Linux 中有时会发生这样一种情况: 一个程序崩溃, 并且处于死锁的状态. 此时一般不用重新启动计算机, 只需要中止(或者说是关闭)这个有问题的程序即可。

当 `kill` 处于 `X-Window` 界面时, 主要的程序(除了崩溃的程序之外)一般都已经正常启动了. 此时打开一个终端, 在那里中止有问题的程序. 

比如, 如果 `Mozilla` 浏览器程序出现了锁死的情况, 可以使用 kill 命令来中止所有包含有 Mozolla 浏览器的程序. 首先用 `top` 命令查处该程序的 `PID`, 然后使用 kill 命令停止这个程序：

```bash
kill -SIGKILL xxx
``` 

其中, xxx 是无响应进程的标识号
