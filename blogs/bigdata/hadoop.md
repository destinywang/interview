# 1. 大数据概述

1. volume: 大量数据
2. variety: 包括数据源和数据类型多样化
3. value: 挖掘数据的价值
4. velocity: 处理速度

![](http://oetw0yrii.bkt.clouddn.com/18-9-8/70277078.jpg)

## 1.1 大数据涉及到的技术
1. 数据采集
2. 数据存储
3. 数据处理/分析/挖掘
4. 数据可视化

## 1.2 大数据在技术架构上带来的挑战
1. 对现有数据库管理技术的挑战
2. 经典数据库并没有考虑到数据的多类别
3. 实时性的技术挑战
4. 对于网络架构 / 数据中心 / 运维的挑战

# 2. Hadoop
> Hadoop 是一个开源的可靠可扩展的分布式计算框架  
允许分布式地处理大数据集, 能够从单台 server 扩展到上千个节点

Hadoop 主要包括的模块:
模块名| 作用
---|---
Hadoop Common | 提供一些对于 Hadoop 其他框架尽心支持的工具类
Hadoop Distributed File System(HDFS) | 一个分布式文件系统, 能够提供较高的吞吐量
Hadoop Yarn | 提供作业调度和集群资源管理
Hadoop MapReduce | 基于 Yarm 之上的能够并行处理大数据集的框架

## 2.1 HDFS(分布式文件系统)

![](http://oetw0yrii.bkt.clouddn.com/18-9-9/95678873.jpg)

> 源自于 Google GFS 相关论文;  
HDFS 是 GFS 的开源版本;  
特点: 扩展性 / 容错性 / 海量存储;

- 将文件切分成指定大小的数据块(默认 128M)并以多副本的形式存储在多个节点上;
- 数据切分, 多副本, 容错等操作对用户来说是透明的;

## 2.2 YARN(资源调度系统)

![](http://oetw0yrii.bkt.clouddn.com/18-9-9/2869565.jpg)

> Yet Another Resource Negotiator  
负责资源的管理和调度
特点: 扩展性 / 容错性 / 多框架资源统一调度

在 HDFS 之上, YARN 的角色是资源管理系统

## 2.3 分布式计算框架 MapReduce

![](http://oetw0yrii.bkt.clouddn.com/18-9-9/29518710.jpg)

> 源自于 Google 的 MapReduce 论文;  
MapReduce 是 Google MapReduce 的克隆版;  
特点: 扩展性 / 容错性 / 海量数据离线处理;

## 2.4 狭义与广义的概念

![](http://oetw0yrii.bkt.clouddn.com/18-9-9/55059217.jpg)

- 狭义 Hadoop: 是一个适合大数据分布式存储(HDFS), 分布式计算(MapReduce)和资源调度(Yarn)的平台.
- 广义的Hadoop: 指的是 Hadoop 生态系统, Hadoop 生态系统是一个很庞大的概念, 其中的每一个子系统只能解决某一个特定的问题, 是小而精的多个小系统.

 

子系统 | 主要功能
:-:|---
HDFS |  Hadoop 分布式文件系统, 以多副本高容错的方式实现数据存储
MapReduce | 分布式计算框架
Hive | 定义了类似 SQL 的语法 `HiveSQL`, 可以将 SQL 语句借助于 Hive 的执行引擎, <br>转换为 MapReduce 任务提交到集群中进行运算, 适用于离线分析
Pig | 将脚本转化为 MapReduce 任务, 提交到集群中进行运算
Oozie | 作业调度系统, 可以定义作业之间的依赖关系
Flume | 日志收集系统, 将集群所有节点的日志收集到 HDFS 中
Sqoop | 实现关系型数据库和 Hadoop 集群的数据传输
HBase | 列式存储数据库

## 2.5 Hadoop 生态系统的特点
1. 开源, 社区活跃
2. 囊括了大数据处理的主要场景

# 3. HDFS