# 1. binlog 定义
> binlog 是记录所有数据库表结构变更(例如CREATE, ALTER TABLE...)以及表数据修改(INSERT, UPDATE, DELETE...)的二进制日志.  
binlog 不会记录 SELECT 和 SHOW 这类操作, 因为这类操作对数据本身并没有修改, 但你可以通过查询通用日志来查看 MySQL 执行过的所有语句.

二进制日志包含两类文件:
1. 索引文件(文件名后缀 `.index`), 用于记录哪些日志文件正在被使用;
2. 日志文件(文件名后缀为 `.00000*`), 记录数据库所有的 DDL 和 DML(除了数据查询语句)语句事件.

假设文件 `my.cnf` 有以下配置:
```
log_bin=ON                                      # 打开 binlog 日志
log_bin_basename=/var/lib/mysql/mysql-bin       # binlog 日志的基本文件名, 后面会追加标识来表示每一个文件
log_bin_index=/var/lib/mysql/mysql-bin.index    # 指定 binlog 文件的索引文件, 这个文件管理了所有的 binlog 文件的目录
```

那么 MySQL 启动后, 会在目录文件 `/var/lib/mysql/` 下出现两个文件:
- `mysql-bin.000001`:
- `mysql-bin.index`: 索引文件

# 2. 用途