# GraphQL

    GraphQL 是一种 API 查询语言.
    
我们在使用REST接口时, 接口返回的数据格式, 数据类型都是后端预先定义好的,   
如果返回的数据格式并不是调用者所期望的, 前端可以通过以下两种方式来解决问题:
1. 和后端沟通，改接口（更改数据源）
2. 自己做一些适配工作（处理数据源）

如果是公司项目, 改后端接口往往是一件比较敏感的事情, 尤其是对于三端(web, andriod, ios)共用同一套后端接口的情况. 大部分情况下, 均是按第二种方式来解决问题的.

因此如果接口的返回值，可以通过某种手段，从静态变为动态，即调用者来声明接口返回什么数据，很大程度上可以进一步解耦前后端的关联。

在GraphQL中，我们通过预先定义一张Schema和声明一些Type来达到上面提及的效果，我们需要知道：
- 对于数据模型的抽象是通过Type来描述的;
- 对于接口获取数据的逻辑是通过Schema来描述的.

# 2. Type

    对于数据模型的抽象是通过 Type 来描述的, 每一个 Type 有若干 Field 组成, 每个 Field 又分别指向某个 Type.
    
GraphQL 的 Type 简单可以分为两种, 一种叫做 `Scalar Type(标量类型)`, 另一种叫做 `Object Type(对象类型)`.
    
## 2.1 Scalar Type
GraphQL中的内建的标量包含, `String`, `Int`, `Float`, `Boolean`, `Enum`;

值得注意的是, GraphQL 中可以通过 `Scalar` 声明一个新的标量, 比如:

`prisma`（一个使用GraphQL来抽象数据库操作的库）中，还有 `DateTime` 和 `ID` 这两个标量分别代表日期格式和主键
在使用 GraphQL 实现文件上传接口时, 需要声明一个 `Upload` 标量来代表要上传的文件
总之，我们只需要记住，标量是 GraphQL 类型系统中最小的颗粒, 关于它在 GraphQL 解析查询结果时, 我们还会再提及它.  

## 2.2 Object Type
仅有标量是不够的抽象一些复杂的数据模型的，这时候我们需要使用对象类型

```
type Article {
    id: ID
    text: String
    isPublished: Boolean
}
```

上面的代码，就声明了一个 `Article` 类型，它有 3 个 `Field`, 分别是 ID 类型的 `id`, String 类型的 `text` 和 Boolean 类型的 `isPublished`.

对于对象类型的 Field 的声明, 我们一般使用标量, 但是我们也可以使用另外一个对象类型, 比如如果我们再声明一个新的 `User` 类型, 如下:
```
type User {
    id: ID
    name: String
}
```

这时我们就可以稍微的更改一下关于Article类型的声明代码，如下：
```
type Article {
    id: ID
    text: String
    isPublished: Boolean
    author: User
}
```

`Article` 新增的 `author` 的 Field 是 User 类型, 代表这篇文章的作者.

总之，我们通过对象模型来构建 GraphQL 中关于一个数据模型的形状, 同时还可以声明各个模型之间的内在关联(一对多, 一对一或多对多)。

## 2.3 Type Modifier

关于类型, 还有一个较重要的概念, 即类型修饰符, 当前的类型修饰符有两种, 分别是 `List` 和 `Required`, 它们的语法分别为 `[Type]` 和 `Type!`

同时这两者可以互相组合, 比如 `[Type]!` 或者 `[Type!]` 或者 `[Type!]!`(请仔细看这里!的位置)，它们的含义分别为：
- 列表本身为必填项，但其内部元素可以为空
- 列表本身可以为空，但是其内部元素为必填
- 列表本身和内部元素均为必填

我们进一步来更改上面的例子，假如我们又声明了一个新的 `Comment` 类型，如下：
```
type Comment {
    id: ID!
    desc: String,
    author: User!
}
```

你会发现这里的 `ID` 有一个 `!`, 它代表这个 `Field` 是必填的, 再来更新 `Article` 对象, 如下:
```
type Article {
    id: ID!
    text: String
    isPublished: Boolean
    author: User!
    comments: [Comment!]
}
```

我们这里的作出的更改如下：
- `id` 字段改为必填
- `author` 字段改为必填
- 新增了 `comments` 字段, 它的类型是一个元素为 `Comment` 类型的 List 类型

最终的 `Article` 类型，就是 GraphQL 中关于文章这个数据模型，一个比较简单的类型声明。

# 3. Schema


    是用来描述对于接口获取数据逻辑的

但这样描述仍然是有些抽象的，我们其实不妨把它当做 REST 架构中每个独立资源的 uri 来理解它, 只不过在 GraphQL 中, 我们用 Query 来描述资源的获取方式. 因此, 我们可以将 Schema 理解为多个 Query 组成的一张表。

这里又涉及一个新的概念 Query, GraphQL 中使用 Query 来抽象数据的查询逻辑, 当前标准下, 有三种查询类型, 分别是 `query（查询）`, `mutation（更改）` 和 `subscription（订阅）`.

> 为了方便区分，Query特指GraphQL中的查询（包含三种类型），query指GraphQL中的查询类型（仅指查询类型）

## 3.1 Query

上面所提及的3中基本查询类型是作为Root Query（根查询）存在的，对于传统的CRUD项目，我们只需要前两种类型就足够了，第三种是针对当前日趋流行的real-time应用提出的。

我们按照字面意思来理解它们就好，如下：

- query（查询）：当获取数据时，应当选取Query类型
- mutation（更改）：当尝试修改数据时，应当使用mutation类型
- subscription（订阅）：当希望数据更改时，可以进行消息推送，使用subscription类型


首先，我们分别以 REST 和 GraphQL 的角度，以Article 为数据模型，编写一系列 CRUD 的接口，如下：

Rest 接口

    GET /api/v1/articles/
    GET /api/v1/article/:id/
    POST /api/v1/article/
    DELETE /api/v1/article/:id/
    PATCH /api/v1/article/:id/
    
GraphQL Query

    query {
        articles(): [Article!]!
        article(id: Int): Article!
    }

    mutation {
        createArticle(): Article!
        updateArticle(id: Int): Article!
        deleteArticle(id: Int): Article!
    }
    
对比我们较熟悉的 REST 的接口我们可以发现, GraphQL 中是按根查询的类型来划分 Query 职能的  
同时还会明确的声明每个 Query 所返回的数据类型，这里的关于类型的语法和上一章节中是一样的。需要注意的是，我们所声明的任何Query都必须是 Root Query 的子集，这和GraphQL内部的运行机制有关。

例子中我们仅仅声明了 Query 类型和 Mutation 类型，如果我们的应用中对于评论列表有 real-time 的需求的话，在 REST 中，我们可能会直接通过长连接或者通过提供一些带验证的获取长连接 url 的接口，比如：

    POST /api/v1/messages/

之后长连接会将新的数据推送给我们，在GraphQL中，我们则会以更加声明式的方式进行声明，如下

    subscription {
        updatedArticle() {
            mutation
            node {
                comments: [Comment!]!
            }
        }
    }
    
## 3.2 Resolver
如果我们仅仅在Schema中声明了若干 Query，那么我们只进行了一半的工作，因为我们并没有提供相关 Query 所返回数据的逻辑。为了能够使 GraphQL 正常工作，我们还需要再了解一个核心概念，Resolver（解析函数）。

GraphQL中，我们会有这样一个约定，Query 和与之对应的 Resolver 是同名的，这样在 GraphQL 才能把它们对应起来，举个例子，比如关于`articles(): [Article!]!`这个 Query, 它的 Resolver 的名字必然叫做 `articles`。

在介绍 Resolver 之前，是时候从整体上了解下GraphQL的内部工作机制了，假设现在我们要对使用我们已经声明的 articles 的 Query，我们可能会写以下查询语句（同样暂时忽略语法）：

    Query {
        articles {
            id
            author {
                name
            }
            comments {
                id
                desc
                author
            }
        }
    }
    
GraphQL在解析这段查询语句时会按如下步骤（简略版）：

1. 首先进行第一层解析，当前 Query 的 Root Query 类型是 query，同时需要它的名字是 `articles`
2. 之后会尝试使用 `articles` 的 Resolver 获取解析数据，第一层解析完毕
3. 之后对第一层解析的返回值，进行第二层解析，当前 `articles` 还包含三个子 Query，分别是 `id`、`author` 和 `comments`
    1. `id` 在 `Author` 类型中为标量类型，解析结束
    2. `author` 在 `Author` 类型中为对象类型 `User`，尝试使用 `User` 的 Resolver 获取数据，当前 `field` 解析完毕
    3. 之后对第二层解析的返回值，进行第三层解析，当前 `author` 还包含一个Query, `name`，由于它是标量类型，解析结束
    4. `comments` 同上
    
GraphQL大体的解析流程就是遇到一个Query之后，尝试使用它的Resolver取值，之后再对返回值进行解析，这个过程是递归的，直到所解析Field的类型是 `Scalar Type（标量类型）` 为止。解析的整个过程我们可以把它想象成一个很长的 `Resolver Chain（解析链）`。

Resolver本身的声明在各个语言中是不一样的，因为它代表数据获取的具体逻辑。它的函数签名(以js为例子)如下：

```js
function(parent, args, ctx, info) {
    ...
}
```
    
其中的参数的意义如下：

- parent: 当前上一个Resolver的返回值
- args: 传入某个Query中的函数（比如上面例子中article(id: Int)中的id）
- ctx: 在Resolver解析链中不断传递的中间变量（类似中间件架构中的context）
- info: 当前Query的AST对象

值得注意的是，Resolver 内部实现对于 GraphQL 完全是黑盒状态。这意味着 Resolver 如何返回数据、返回什么样的数据、从哪返回数据，完全取决于Resolver本身，基于这一点，在实际中，很多人往往把GraphQL作为一个中间层来使用，数据的获取通过Resolver来封装，内部数据获取的实现可能基于RPC、REST、WS、SQL等多种不同的方式。同时，基于这一点，当你在对一些未使用GraphQL的系统进行迁移时（比如REST），可以很好的进行增量式迁移。