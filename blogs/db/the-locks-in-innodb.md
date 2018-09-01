概述
1. 共享/排他锁(Shared and Exclusive Locks)
2. 易向锁(Intention Locks)
3. 记录锁(Record Locks)
4. 间隙锁(Gap Locks)
5. 临键锁(Next-key Locks)
6. 插入意向锁(Insert Intention Locks)
7. 自增锁(Auto-inc Locks)

# 1. 自增锁
在 InnoDB 中, 有如下表:
```sql
tb(id auto_increment primary key, name);
```

表中有如下数据:

    (1, 'freedom')
    (2, 'justice')
    (3, 'destiny')
    
然后进行如下操作: 

|事务 A|事务 B|
|---|---|
|`INSERT INTO tb VALUES (4, 'F91')` | - |
| - |`INSERT INTO tb VALUES (5, 'RX-87')` |
| `commit` | - |
| - | `commit` |

此时, 在事务 A 提交之前, 事务 B 会一直阻塞.

> 自增锁是一种特殊的表锁, 专门针对 `auto_increment` 的列, 如果一个事务正在向表中插入记录, 其他事务必须阻塞, 以便第一个事务插入的行, 是连续的主键.

# 2. 共享/排他锁
InnoDB 实现了标准的行锁:
1. 事务拿到某一行记录的共享S锁, 才可以读取这一行;
2. 事务拿到某一行记录的排它X锁, 才可以修改或者删除这一行;

> 多个事务可以拿到一把S锁, `读读` 可以并行;  
只有一个事务可以拿到X锁, `写写`/`读写` 必须互斥

# 3. 意向锁
InnoDB 支持 `多粒度锁(multiple granularity locking)`, 它允许行级锁与表级锁共存, 实际应用中, InnoDB 使用的是意向锁

意向锁是指, 未来的某个时刻, 事务可能要加 `共享/排它锁` 了, 先提前声明一个意向
1. 意向锁, 是一个表级别的锁(table-level locking)
2. 意向锁分为:
    1. `意向共享锁(intention shared lock, IS)`, 它预示着, 事务有意向对表中的某些行加共享S锁
    2. `意向排它锁(intention exclusive lock, IX)`, 它预示着, 事务有意向对表中的某些行加排它X锁
    
意向锁协议:
- 事务要获得某些行的S锁, 必须先获得表的IS锁;
- 事务要获得某些行的X锁, 必须先获得表的IX锁
    
# 4. 插入意向锁
> 对已有数据行的修改与删除, 必须加强互斥锁X锁, 那对于数据的插入, 是否还需要加这么强的锁, 来实施互斥呢?

答案是不需要, 因此有了 `插入意向锁`.

插入意向锁, 是 `间隙锁(Gap Locks)` 的一种 `(一种实施在索引上, 锁定索引某个区间范围的锁)`, 它是专门针对 `insert` 操作的.

    多个事务, 在同一个索引, 同一个范围区间插入记录时, 如果插入的位置不冲突, 不会阻塞彼此.
    
# 5. 记录锁
记录锁, 它封锁索引记录, 例如: 
```sql
select * from tb where id = 1 for update;
```

它会在 id = 1 的索引记录上加锁, 以阻止其他事务插入，更新, 删除 `id = 1` 的这一行.

需要说明的是:

```sql
select * from t where id = 1;
```

则是 `快照读(SnapShot Read)`，它并不加锁

# 6. 间隙锁
> 间隙锁, 它封锁索引记录中的间隔, 或者第一条索引记录之前的范围, 又或者最后一条索引记录之后的范围.

```sql
tb(id primary key, name KEY, sex, flag);
```

表中有 4 条记录:

    (1, shenjian, m, A)
    (3, zhangsan, m, A)
    (5, lisi, m, A)
    (9, wangwu, f, B)
    
而 SQL 语句:
```sql
select * from tb where id between 8 and 15 for update;
```

会封锁区间，以阻止其他事务 `如id=10` 的记录插入。

如果能够插入成功, 头一个事务执行相同的SQL语句, 会发现结果集多出了一条记录, 即 `幻影数据`.

间隙锁的主要目的, 就是为了防止其他事务在间隔中插入数据, 以导致 `不可重复读`.

如果把事务的隔离级别降级为 `读已提交(Read Committed, RC)`, 间隙锁则会自动失效.

# 7. 临键锁
临键锁, 是记录锁与间隙锁的组合, 它的封锁范围, 既包含索引记录, 又包含索引区间.

更具体的, 临键锁会封锁索引记录本身, 以及索引记录之前的区间.

如果一个会话占有了索引记录 `R` 的共享/排他锁, 其他会话不能立刻在 `R` 之前的区间插入新的索引记录.

临键锁的主要目的, 也是为了避免 `幻读(Phantom Read)`, 如果把事务的隔离级别降级为RC, 临键锁则也会失效.