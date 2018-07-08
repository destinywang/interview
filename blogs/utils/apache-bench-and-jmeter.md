# 1. Apache Bench
## 1.1 简介
`Apache Bench` 是一种用于测试 Apache 超文本传输协议（HTTP）服务器的工具。 apache 自带 ab 工具，可以测试
apache、IIs、tomcat、nginx 等服务器.

但是 ab 没有 `Jmeter`, `Loadrunner` 那样有各种场景设计、各种图形报告和监控，只需一个命令即可，有输出描述
可以简单的进行一些压力测试

Mac 自带 apache  
查看版本:

```bash
$ apachectl -v
Server version: Apache/2.4.33 (Unix)
Server built:   Apr  3 2018 18:00:56
```

## 1.2 简单使用
假设有如下接口:
```java
@RequestMapping("/test")
public String test() {
    return "test";
}
```

基本用法:
```bash
$ ab -n 全部请求数 -c 并发数 测试URL
```

```bash
$  ab -n 1000 -c 50 http://localhost:8080/test
This is ApacheBench, Version 2.3 <$Revision: 1826891 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking localhost (be patient)
Completed 100 requests
Completed 200 requests
Completed 300 requests
Completed 400 requests
Completed 500 requests
Completed 600 requests
Completed 700 requests
Completed 800 requests
Completed 900 requests
Completed 1000 requests
Finished 1000 requests


Server Software:
Server Hostname:        localhost
Server Port:            8080

Document Path:          /test
Document Length:        4 bytes             # 响应正文大小

Concurrency Level:      50                  # 并发数
Time taken for tests:   0.198 seconds       # 全部请求完成耗时
Complete requests:      1000                # 完成请求数
Failed requests:        0                   # 失败的请求数
Total transferred:      136000 bytes        # 所有请求响应数据总和(包括头信息 + 正文信息)
HTML transferred:       4000 bytes          # 所有请求响应数据中的正文总和
Requests per second:    5050.71 [#/sec] (mean)      # 吞吐率, (Complete requests) / (Time taken for tests)
Time per request:       9.900 [ms] (mean)           # 用户平均请求等待时间
Time per request:       0.198 [ms] (mean, across all concurrent requests)   # 服务器平均请求等待时间
Transfer rate:          670.80 [Kbytes/sec] received                        # 请求在单位时间内从服务器获取的数据长度 (Total transferred) / (Time taken for tests)

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    1   1.3      1       6
Processing:     1    8   6.0      7      50
Waiting:        1    6   4.0      5      34
Total:          1   10   5.9      8      52

Percentage of the requests served within a certain time (ms)
  50%      8
  66%     10
  75%     11
  80%     12
  90%     15
  95%     22
  98%     29
  99%     32
 100%     52 (longest request)
```

# 2. Jmeter
![](http://oetw0yrii.bkt.clouddn.com/18-7-8/26052954.jpg)

运行 `jmeter.sh` 启动界面

添加线程组
![](http://oetw0yrii.bkt.clouddn.com/18-7-8/87665889.jpg)

设置线程组
![](http://oetw0yrii.bkt.clouddn.com/18-7-8/1829110.jpg)
- 线程数: 模拟用户数
- 虚拟用户增长时长: 在指定的秒数中完成操作
- 循环次数: 一个虚拟用户执行次数

添加 HTTP 请求
![](http://oetw0yrii.bkt.clouddn.com/18-7-8/17026331.jpg)

设置 HTTP 请求, 并添加监听器
![](http://oetw0yrii.bkt.clouddn.com/18-7-8/50380382.jpg)
