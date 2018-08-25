### 1. 查看总内存
```bash
$ MEM_SIZE = free | grep Mem | awk '{print $2}'
```
free 命令的返回值:
```
             total       used       free     shared    buffers     cached
Mem:      16331692   16084640     247052        100     289164    6609192
-/+ buffers/cache:    9186284    7145408
Swap:      4191228    1079276    3111952
```

`free | grep Mem | awk '{print $2}'` 是找到 `Mem` 行的第二列数据, 即 `total`, 总内存信息

### 2. 根据总内存判断
```bash
if [ $MEM_SIZE -gt 4000000 ]
then
        HEAP_SIZE="2048m"
else
        HEAP_SIZE="1024m"
fi
```
如果总内存 > 4000000, 将堆大小设置为 2048M, 否则将堆大小设置为 1024M

### 3. JVM 参数设置
```bash
CATALINA_OPTS=" $CAESAR_OPTS      
    -Dorg.apache.jasper.compiler.Parser.STRICT_QUOTE_ESCAPING=false 
    -Xms$HEAP_SIZE 
    -Xmx$HEAP_SIZE 
    -XX:PermSize=128m 
    -XX:MaxPermSize=256m
    -Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.EPollSelectorProvider 
    -Djava.library.path=$CATALINA_HOME/lib/native 
    -verbose:gc 
    -XX:+PrintGCTimeStamps 
    -XX:NewRatio=3 
    -XX:SurvivorRatio=8 
    -XX:MaxTenuringThreshold=7 
    -XX:GCTimeRatio=19 
    -XX:ReservedCodeCacheSize=256m 
    -XX:+UseParNewGC 
    -XX:+UseConcMarkSweepGC 
    -Dspring.profiles.active=online 
    -Dinitmemcached=false"
```

参数名 | 参数含义
---|---
-Xms | 初始堆大小: 6G
-Xmx | 最大堆大小: 6G
-XX:PermSize=128m | 永久带初始内存分配大小: 128M
-XX:MaxPermSize=256m | 永久代对象能占用内存的最大值: 256m
-verbose:gc | 输出每次GC的相关情况
-XX:+PrintGCTimeStamps | 打印每次GC的时间戳
-XX:NewRatio=3 | 设置`新生代`和`老年代`的比例，比如值为3，则`老年代`是`新生代`的2倍，, 即`新生代`占据内存的1/4
-XX:SurvivorRatio=8 | 设置`Eden`和一个`Suivior`的比例, 比如值为8，即Eden是To(S2)的比例是8，(From和To是一样大的)，此时Eden占据Yong Generation的8/10
-XX:MaxTenuringThreshold | 用于控制对象能经历多少次`Minor GC`才晋升到老年代
-XX:GCTimeRatio | 设置吞吐量大小, 假设 GCTimeRatio 的值为 n，那么系统将花费不超过 1/(1+n) 的时间用于垃圾收集
-XX:ReservedCodeCacheSize | 保留代码占用的内存容量: 256M
-XX:+UseParNewGC | 设置年轻代为多线程收集。可与CMS收集同时使用。在serial基础上实现的多线程收集器。
-XX:+UseConcMarkSweepGC | 设置年老代为并发收集。此时年轻代大小最好用-Xmn设置。
