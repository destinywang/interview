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

> 源自于 Google GFS 相关论文;  
HDFS 是 GFS 的开源版本;  
特点: 扩展性 / 容错性 / 海量存储;

- 将文件切分成指定大小的数据块(默认 128M)并以多副本的形式存储在多个节点上;
- 数据切分, 多副本, 容错等操作对用户来说是透明的;

## 2.2 YARN(资源调度系统)

> Yet Another Resource Negotiator  
负责资源的管理和调度
特点: 扩展性 / 容错性 / 多框架资源统一调度

在 HDFS 之上, YARN 的角色是资源管理系统

## 2.3 分布式计算框架 MapReduce
> 源自于 Google 的 MapReduce 论文;  
MapReduce 是 Google MapReduce 的克隆版;  
特点: 扩展性 / 容错性 / 海量数据离线处理;

