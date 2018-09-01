
假设有如下表:
```sql
tb(id primary key, name);
```

表中有三条记录:

    (1, 'freedom')
    (2, 'justice')
    (3, 'destiny')

# 1. 脏读
|事务 A|事务 B|
|:-:|:-:|
|`INSERT INTO tb VALUES (4, 'F91')` | - |
| - | `SELECT * FROM tb`|
| `commit` | - |

如果事务 B 能够读取到 (4, 'F91') 这条记录, 事务 A 就对事务 B 产生了影响, 这个影响叫做 `脏读`, 读到了未提交事务操作的记录.

脏读是由`读未提交(Read Uncommitted)` 造成的, 这是并发度最高, 但一致性最差的隔离级别.

在这种事务隔离级别下, SELECT 语句不加锁, 可能在读取的过程中数据遭到修改;

# 2. 不可重复读
|事务 A|事务 B|
|:-:|:-:|
|`SELECT * FROM tb where id = 1` | - |
| 返回(1, 'freedom') | - |
| - | `UPDATE tb SET name = 'gundam' where id = 1;` |
| - | `commit` |
| `SELECT * FROM tb where id = 1` | - |
| `commit` | - |

如果事务 A 的结果是 (1, 'gundam'), 则说明事务 B 对事务 A 产生了影响, 这个影响叫做 `不可重复读`, 一个事务内的两次相同查询, 可能会得到不同的结果;

不可重复读是由`读已提交(Read Committed)` 造成的, 在该隔离级别下:
1. 普通读取是快照读;
2. 加锁的 `SELECT`, `UPDATE`, `DELETE` 等语句, 除了在外键约束检查以及重复性检查时会封锁区间, 其他时候都只使用记录锁;


# 3. 幻读
|事务 A|事务 B|
|:-:|:-:|
|`SELECT * FROM tb where id > 3| - |
| `commit `| - |
| 返回 NULL | - |
| - | `INSERT INTO tb VALUES (4, 'F91') | 
| - | `commit` |
| - | 返回 Error : duplicate key!  |

幻读是由 `可重复读(Repeated Read)` 造成的, 在这种隔离级别下:
1. 普通的 `SELECT` 使用快照读
2. 加锁的 `SELECT(select ... in share mode, select ... for update)`, `UPDATE`, `DELETE` 等语句, 他们的锁, 依赖于它们是否在 `唯一索引(unique index)` 上使用了 `唯一的查询条件(unique search condition)`, 或者 `范围查询条件(range-type search condition)`:
    1. 在唯一索引上使用唯一的查询条件, 会使用 `记录锁(record lock)` , 而不会封锁记录之间的间隔, 即不会使用 `间隙锁(gap lock)` 与 `临键锁(next-key lock)`
    2. 范围查询条件, 会使用间隙锁与临键锁, 锁住索引记录之间的范围, 避免范围间插入记录, 以避免产生幻影行记录, 以及避免不可重复的读
    