> 初识 `贫血模型` 与 `充血模型`, 是在《轻量级J2EE开发实践》中, 它们是面向对象程序设计对实体 (Entity) 建模的两种方式. 
对于需求分析得到的 Entity, 首先面临的问题是如何构建 `Domain Object(领域模型)`. `贫血模型` 与 `充血模型` 给出了两种不同的方案：

- 贫血模型: 是指领域对象里只有 `getter()` 和 `setter()` 方法, 或者包含少量的 `CRUD` 方法, 所有的业务逻辑都不包含在内而是放在 `Business Logic` 层.
- 充血模型: 层次结构和上面的差不多, 不过大多业务逻辑和持久化放在 `Domain Object` 里面, `Business Logic(业务逻辑层)`只是简单封装部分业务逻辑以及控制事务, 权限等.


    简而言之, 贫血模型下, Domain Object 只是个保存相关属性的马甲, 其中的内容需要 Business Logic 注入;
    而充血模型下，Domain Object 既有肉体 (属性) 也有灵魂 (业务逻辑), Business Logic 只是其逻辑的简单封装.
    
# 1. 失血模型

	失血模型简单来说, 就是 domain object 只有属性的 getter/setter 方法的纯数据类, 所有的业务逻辑完全由 business object 来完成(又称TransactionScript), 这种模型下的 domain object 被 Martin Fowler 称之为 "贫血的 domain object"。 

代码实例
---

	一个实体类叫做Item，指的是一个拍卖项目 
	一个DAO接口类叫做ItemDao 
	一个DAO接口实现类叫做ItemDaoHibernateImpl 
	一个业务逻辑类叫做ItemManager(或者叫做ItemService) 

```java
public class Item implements Serializable { 
     private Long id = null; 
     private int version; 
     private String name; 
     private User seller; 
     private String description; 
     private MonetaryAmount initialPrice; 
     private MonetaryAmount reservePrice; 
     private Date startDate; 
     private Date endDate; 
     private Set categorizedItems = new HashSet(); 
     private Collection bids = new ArrayList(); 
     private Bid successfulBid; 
     private ItemState state; 
     private User approvedBy; 
     private Date approvalDatetime; 
     private Date created = new Date(); 
     //   getter/setter方法省略不写，避免篇幅太长 
}

// ItemDao 定义持久化操作的接口，用于隔离持久化代码。 
public interface ItemDao { 
     Item getItemById(Long id); 
     Collection findAll(); 
     void updateItem(Item item); 
}

// ItemDaoHibernateImpl 完成具体的持久化工作
public class ItemDaoHibernateImpl implements ItemDao extends HibernateDaoSupport { 
     public Item getItemById(Long id) { 
         return (Item) getHibernateTemplate().load(Item.class, id); 
     } 
     public Collection findAll() { 
         return (List) getHibernateTemplate().find("from Item"); 
     } 
     public void updateItem(Item item) { 
         getHibernateTemplate().update(item); 
     } 
}

// 事务的管理是在ItemManger这一层完成的，ItemManager实现具体的业务逻辑。除了常见的和CRUD有关的简单逻辑之外，这里还有一个 placeBid 的逻辑，即项目的竞标。 
public class ItemManager { 
     private ItemDao itemDao; 
     public void setItemDao(ItemDao itemDao) { this.itemDao = itemDao;} 
     public Bid loadItemById(Long id) { 
         itemDao.loadItemById(id); 
     } 
     public Collection listAllItems() { 
         return   itemDao.findAll(); 
     } 
     public Bid placeBid(Item item, User bidder, MonetaryAmount bidAmount, 
                             Bid currentMaxBid, Bid currentMinBid) throws BusinessException { 
             if (currentMaxBid != null && currentMaxBid.getAmount().compareTo(bidAmount) > 0) { 
             throw new BusinessException("Bid too low."); 
     } 
    
     // Auction is active 
     if ( !state.equals(ItemState.ACTIVE) ) 
             throw new BusinessException("Auction is not active yet."); 
    
     // Auction still valid 
     if ( item.getEndDate().before( new Date() ) ) 
             throw new BusinessException("Can't place new bid, auction already ended."); 
    
     // Create new Bid 
     Bid newBid = new Bid(bidAmount, item, bidder); 
    
     // Place bid for this Item 
     item.getBids().add(newBid); 
     itemDao.update(item);      //   调用DAO完成持久化操作 
     return newBid; 
     } 
}
```

以上是一个完整的第一种模型的示例代码。在这个示例中，`placeBid`，`loadItemById`，`findAll` 等等业务逻辑统统放在 `ItemManager中实现`，而 `Item` 只有 `getter/setter` 方法。



# 2. 贫血模型

	简单来说，就是 domain ojbect 包含了不依赖于持久化的领域逻辑，而那些依赖持久化的领域逻辑被分离到Service层。 
	Service(业务逻辑，事务封装) --> DAO ---> domain object 这也就是Martin Fowler指的rich domain object 

代码实例:
---

	一个带有业务逻辑的实体类，即domain object是Item 
	一个DAO接口ItemDao 
	一个DAO实现ItemDaoHibernateImpl 
	一个业务逻辑对象ItemManager 

```java
// 竞标这个业务逻辑被放入到Item中来。请注意this.getBids.add(newBid); 如果没有Hibernate或者JDO这种O/R Mapping的支持，我们是无法实现这种透明的持久化行为的。但是请注意，Item里面不能去调用ItemDAO，对ItemDAO产生依赖！
public class Item implements Serializable { 
    //   所有的属性和getter/setter方法同上，省略 
    public Bid placeBid(User bidder, MonetaryAmount bidAmount, 
                         Bid currentMaxBid, Bid currentMinBid) 
             throws BusinessException { 
        // Check highest bid (can also be a different Strategy (pattern)) 
        if (currentMaxBid != null && currentMaxBid.getAmount().compareTo(bidAmount) > 0) { 
            throw new BusinessException("Bid too low."); 
        }    
        // Auction is active 
        if ( !state.equals(ItemState.ACTIVE) ) 
            throw new BusinessException("Auction is not active yet.");     
        // Auction still valid 
        if ( this.getEndDate().before( new Date() ) ) 
            throw new BusinessException("Can't place new bid, auction already ended."); 
        // Create new Bid 
        Bid newBid = new Bid(bidAmount, this, bidder);    
        // Place bid for this Item 
        this.getBids.add(newBid);   // 请注意这一句，透明的进行了持久化，但是不能在这里调用ItemDao，Item不能对ItemDao产生依赖！ 
    	return newBid; 
    } 
}

 

// ItemDao和ItemDaoHibernateImpl的代码同上，省略。 

public class ItemManager { 
    private ItemDao itemDao; 
    public void setItemDao(ItemDao itemDao) { this.itemDao = itemDao;} 
    public Bid loadItemById(Long id) { 
        itemDao.loadItemById(id); 
    } 
    public Collection listAllItems() { 
        return   itemDao.findAll(); 
    } 
    public Bid placeBid(Item item, User bidder, MonetaryAmount bidAmount, 
                            Bid currentMaxBid, Bid currentMinBid) throws BusinessException { 
        item.placeBid(bidder, bidAmount, currentMaxBid, currentMinBid); 
        itemDao.update(item);     // 必须显式的调用DAO，保持持久化 
    } 
}

```

在第二种模型中，placeBid 业务逻辑是放在 Item 中实现的，而 loadItemById 和 findAll 业务逻辑是放在 ItemManager 中实现的。不过值得注意的是，即使 placeBid 业务逻辑放在 Item 中，你仍然需要在 ItemManager 中简单的封装一层，以保证对 placeBid 业务逻辑进行事务的管理和持久化的触发。 

这种模型是 Martin Fowler 所指的真正的 domain model。在这种模型中，有三个业务逻辑方法：placeBid，loadItemById和 findAll，现在的问题是哪个逻辑应该放在 Item 中，哪个逻辑应该放在 ItemManager 中。在我们这个例子中，placeBid 放在 Item 中(但是 ItemManager 也需要对它进行简单的封装)，loadItemById 和 findAll 是放在 ItemManager 中的。 

切分的原则是什么呢？ Rod Johnson提出原则是 `case by case`，可重用度高的，和 domain object 状态密切关联的放在 Item 中，可重用度低的，和 domain object 状态没有密切关联的放在 ItemManager 中。 

经过上面的讨论，如何区分 domain logic 和 business logic，我想提出一个改进的区分原则： 

domain logic 只应该和这一个 domain object 的实例状态有关，而不应该和一批 domain object 的状态有关；

当你把一个logic放到domain object中以后，这个domain object应该仍然独立于持久层框架之外(Hibernate,JDO)，这个domain object仍然可以脱离持久层框架进行单元测试，这个domain object仍然是一个完备的，自包含的，不依赖于外部环境的领域对象，这种情况下，这个logic才是domain logic。 
这里有一个很确定的原则：logic是否只和这个object的状态有关，如果只和这个object有关，就是domain logic；如果logic是和一批domain object的状态有关，就不是domain logic，而是business logic。 

Item的placeBid这个业务逻辑方法没有显式的对持久化ItemDao接口产生依赖，所以要放在Item中。请注意，如果脱离了Hibernate这个持久化框架，Item这个domain object是可以进行单元测试的，他不依赖于Hibernate的持久化机制。它是一个独立的，可移植的，完整的，自包含的域对象。 

而loadItemById和findAll这两个业务逻辑方法是必须显式的对持久化ItemDao接口产生依赖，否则这个业务逻辑就无法完成。如果你要把这两个方法放在Item中，那么Item就无法脱离Hibernate框架，无法在Hibernate框架之外独立存在。

贫血模型的优缺点:

| 优点 | 缺点 |
| --- | ---|
| 1. 各层单向依赖，结构清楚，易于实现和维护  | 1. domain object的部分比较紧密依赖的持久化domain logic被分离到Service层，显得不够OO |
| 2. 设计简单易行，底层模型非常稳定 | 2. Service层过于厚重 

充血模型的优缺点:

# 3. 充血模型

	充血模型和第二种模型差不多，所不同的就是如何划分业务逻辑，即认为，绝大多业务逻辑都应该被放在domain object里面(包括持久化逻辑)，而Service层应该是很薄的一层，仅仅封装事务和少量逻辑，不和DAO层打交道。 
	Service(事务封装) ---> domain object <---> DAO 这种模型就是把第二种模型的domain object和business object合二为一了。

代码实例:
---

	Item：包含了实体类信息，也包含了所有的业务逻辑 
	ItemDao：持久化DAO接口类 
	ItemDaoHibernateImpl：DAO接口的实现类 

```java
// 由于ItemDao和ItemDaoHibernateImpl和上面完全相同，就省略了。 


public class Item implements Serializable { 
     //   所有的属性和getter/setter方法都省略 
    private static ItemDao itemDao; 
    public void setItemDao(ItemDao itemDao) {this.itemDao = itemDao;} 
    
    public static Item loadItemById(Long id) { 
        return (Item) itemDao.loadItemById(id); 
    } 
    public static Collection findAll() { 
        return (List) itemDao.findAll(); 
    } 

    public Bid placeBid(User bidder, MonetaryAmount bidAmount, 
            			Bid currentMaxBid, Bid currentMinBid) 
     		throws BusinessException { 
    
        // Check highest bid (can also be a different Strategy (pattern)) 
        if (currentMaxBid != null && currentMaxBid.getAmount().compareTo(bidAmount) > 0) { 
                throw new BusinessException("Bid too low."); 
        } 
        
        // Auction is active 
        if ( !state.equals(ItemState.ACTIVE) ) 
                throw new BusinessException("Auction is not active yet."); 
        
        // Auction still valid 
        if ( this.getEndDate().before( new Date() ) ) 
                throw new BusinessException("Can't place new bid, auction already ended."); 
        
        // Create new Bid 
        Bid newBid = new Bid(bidAmount, this, bidder); 
        
        // Place bid for this Item 
        this.addBid(newBid); 
        itemDao.update(this);       //   调用DAO进行显式持久化 
        return newBid; 
    } 
}

```

在这种模型中，所有的业务逻辑全部都在 Item 中，事务管理也在 Item 中实现。

1. 事务我是不希望由 Item 管理的，而是由容器或更高一层的业务类来管理。 
2. 如果 Item 不脱离持久层的管理，如 JDO 的 pm，那么 itemDao.update(this); 是不需要的，也就是说Item是在事务过程中从数据库拿出来的，并且声明周期不超出当前事务的范围。 
3. 如果 Item 是脱离持久层，也就是在Item的生命周期超出了事务的范围，那就要必须显示调用 update 或 attach 之类的持久化方法的，这种时候就应该是按 robbin 所说的第2种模型来做。

| 优点 | 缺点 |
| --- | ---|
| 1. 更加符合OO的原则  | 1. DAO和domain object形成了双向依赖，复杂的双向依赖会导致很多潜在的问题。 |
| 2. Service层很薄，只充当Facade的角色，不和DAO打交道。 | 2. 如何划分Service层逻辑和domain层逻辑是非常含混的，在实际项目中，由于设计和开发人员的水平差异，可能导致整个结构的混乱无序。  |
| | 3. 考虑到Service层的事务封装特性，Service层必须对所有的domain object的逻辑提供相应的事务封装方法，其结果就是Service完全重定义一遍所有的domain logic，非常烦琐，而且Service的事务化封装其意义就等于把OO的domain logic转换为过程的Service TransactionScript。该充血模型辛辛苦苦在domain层实现的OO在Service层又变成了过程式，对于Web层程序员的角度来看，和贫血模型没有什么区别了。

# 4. 胀血模型 

	基于充血模型的第三个缺点，有同学提出，干脆取消Service层，只剩下domain object和DAO两层，在domain object的domain logic上面封装事务。 
	domain object(事务封装，业务逻辑) <---> DAO 

胀血模型优缺点:

| 优点 | 缺点
| --- | --- |
| 简化了分层 | 很多不是domain logic的service逻辑也被强行放入domain object ，引起了domain ojbect模型的不稳定
| 也算符合OO | domain object 暴露给 web 层过多的信息，可能引起意想不到的副作用

# 5. 总结

在这四种模型当中，失血模型和胀血模型应该是不被提倡的。而贫血模型和充血模型从技术上来说，都已经是可行的了。但是我个人仍然主张使用贫血模型。其理由： 
1. 参考充血模型第三个缺点，由于暴露给web层程序拿到的还是Service Transaction Script，对于web层程序员来说，底层OO意义丧失了。 
2. 参考充血模型第三个缺点，为了事务封装，Service层要给每个domain logic提供一个过程化封装，这对于编程来说，做了多余的工作，非常烦琐。 
3. domain object和DAO的双向依赖在做大项目中，考虑到团队成员的水平差异，很容易引入不可预知的潜在bug。
4. 如何划分domain logic和service logic的标准是不确定的，往往要根据个人经验，有些人就是觉得某个业务他更加贴近domain，也有人认为这个业务是贴近service的。由于划分标准的不确定性，带来的后果就是实际项目中会产生很多这样的争议和纠纷，不同的人会有不同的划分方法，最后就会造成整个项目的逻辑分层混乱。这不像贫血模型中我提出的按照是否依赖持久化进行划分，这种标准是非常确定的，不会引起争议，因此团队开发中，不会产生此类问题。 
5. 贫血模型的domain object确实不够rich，但是我们是做项目，不是做研究，好用就行了，管它是不是那么纯的OO呢？其实我不同意firebody认为的贫血模型在设计模型和实现代码中有很大跨越的说法。一个设计模型到实现的时候，你直接得到两个类：一个实体类，一个控制类就行了，没有什么跨越。


第一种模型绝大多数人都反对，因此反对理由我也不多讲了。但遗憾的是，我观察到的实际情形是，很多使用Hibernate的公司最后都是这种模型，这里面有很大的原因是很多公司的技术水平没有达到这种层次，所以导致了这种贫血模型的出现。从这一点来说，Martin Fowler的批评声音不是太响了，而是太弱了，还需要再继续呐喊。 

第二种模型就是Martin Fowler一直主张的模型，实际上也是我一直在实际项目中采用这种模型。我没有看过Martin的POEAA，之所以能够自己摸索到这种模型，也是因为从02年我已经开始思考这个问题并且寻求解决方案了，但是当时没有看到Hibernate，那时候做的一个小型项目我已经按照这种模型来做了，但是由于没有O/R Mapping的支持，写到后来又不得不全部改成贫血的domain object，项目做完以后再继续找，随后就发现了Hibernate。当然，现在很多人一开始就是用Hibernate做项目，没有经历过我经历的那个阶段。 

不过我觉得这种模型仍然不够完美，因为你还是需要一个业务逻辑层来封装所有的domain logic，这显得非常罗嗦，并且业务逻辑对象的接口也不够稳定。如果不考虑业务逻辑对象的重用性的话(业务逻辑对象的可重用性也不可能好)，很多人干脆就去掉了xxxManager这一层，在Web层的Action代码直接调用xxxDao，同时容器事务管理配置到Action这一层上来。Hibernate的caveatemptor就是这样架构的一个典型应用。 

第三种模型是我很反对的一种模型，这种模型下面，Domain Object和DAO形成了双向依赖关系，无法脱离框架测试，并且业务逻辑层的服务也和持久层对象的状态耦合到了一起，会造成程序的高度的复杂性，很差的灵活性和糟糕的可维护性。也许将来技术进步导致的O/R Mapping管理下的domain object发展到足够的动态持久透明化的话，这种模型才会成为一个理想的选择。就像O/R Mapping的流行使得第二种模型成为了可能Martin Fowler的Domain Model，或者说我们的第二种模型难道是完美无缺的吗？当然不是，接下来我就要分析一下它的不足，以及可能的解决办法，而这些都来源于我个人的实践探索。 