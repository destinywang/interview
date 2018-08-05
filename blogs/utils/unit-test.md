# 1. 什么是单元测试
> 单元测试时是**开发人员**编写的一小段代码, 用于检验被测代码的一个有明确功能的小模块是否正确

- 通常是用来判断某个类和函数的行为
- 白盒测试
- 开发人员是最大受益者

# 2. 例子
```java
public class Calculator {
    public int evaluate(String expr) {
        // 对 expr 进行解析, 执行运算
        int result = //...
        return result;
    }
}
```

    "10 + 20 - 5" -> 5
    "(20 - 10) * 5 + 6 / 3" -> 52
    
## 2.1 人肉测试
```java
public static void main(String[] args) {
    Calculator cal = new Calculator();
    int result = cal.evaluate("10 + 20 - 5");
    System.out.println(result);
    
    int result = cal.evaluate("(20 - 10) * 5 + 6 / 3");
    System.out.println(result);
}
```

## 2.2 单元测试框架
```java
public class CalculatorTest {
    @Test
    public void testEvaluate1() {
        Calculator cal = new Calculator();
        int result = cal.evaluate("10 + 20 - 5");
        Assert.assertEquals(25, result);
    }
    
    @Test
    public void testEvaluate1() {
        Calculator cal = new Calculator();
        int result = cal.evaluate("(20 - 10) * 5 + 6 / 3");
        Assert.assertEquals(52, result);
    }
}
```

#### 同时, 如果有大量测试类, 可以打包进行测试
- 打包类:
```java
@RunWith(Suite.class)
@Suite.SuiteClasses({
        Test1.class,
        Test2.class,
        Test3.class
})
public class AllTest {

}
```

- 所有测试类:
```java
public class Test1 {

    @Test
    public void test() {
        System.out.println("Test1.test");
    }

}


public class Test2 {

    @Test
    public void test() {
        System.out.println("Test2.test");
    }

}


public class Test3 {

    @Test
    public void test() {
        System.out.println("Test3.test");
    }

}
```
- 运行结果:

![](http://oetw0yrii.bkt.clouddn.com/18-8-5/21854660.jpg)

而每一个子包同时也可以打包其他的测试类  
因此可以通过 Junit 将所有的测试用例组织起来

这样就提供了不同粒度的测试用例组织方式.

#### 特殊方法
```java
public class Test1 {

    /**
     * 每个测试用例执行前都会调用一次
     */
    @Before
    public void setUp() {
        System.out.println("Test1.setUp");
    }

    /**
     * 每个测试用例执行后都会调用一次
     */
    @After
    public void tearDown() {
        System.out.println("Test1.tearDown");
    }

    @Test
    public void test() {
        System.out.println("Test1.test");
    }

}
```

![](http://oetw0yrii.bkt.clouddn.com/18-8-5/30998197.jpg)

# 3. 单元测试的优点
- 验证行为:
    - 保证代码正确性;
    - 回归测试: 即使拖到项目后期, 仍然可以添加新功能, 而不用担心破坏重要功能;
    - 给重构带来保证.
- 设计行为:
    - 测试驱动: 迫使我们从调用者的角度去观察和思考, 迫使我们把代码设计成可测试的, 松耦合的;
- 文档行为:
    - 单元测试是一种无价的文档, 精确地描述了代码的行为, 是如何使用函数和类的最佳文档
    

# 4. 单元测试的原则
- 测试代码和被测代码是同等重要的, 需要同时被维护
    - 测试代码不是附属品
    - 不但要重构代码, 还需要重构单元测试
- 单元测试一定是隔离的
    - 一个测试用例的运行结果不能影响其他的测试用例
    - 测试用例不能相互依赖, 应该能够以任何次序执行
- 单元测试一定是可以重复运行的
    - 不能依赖环境的变化
- 保持单元测试的简单性和可读性
- 尽量对接口进行测试
- 单元测试应该可以迅速执行
    - 给程序员提供及时反馈
    - 使用 Mock 对象对数据库, 网络的依赖进行解耦
- 自动化单元测试
    - 集成到 build 过程中去

# 5. Mock 对象的时机
- 真实的对象不易构造
    - 例如 `httpServlet` 必须在 `servlet` 容器中才能创造出来
- 真实的对象非常复杂
    - 如 `jdbc` 中的 `connection`, `ResultSet`
- 真实的对象的行为具有不确定性, 难于控制他们的输出或者返回结果
- 真实的对象有些行为难于触发, 例如磁盘已满, 网络连接断开
- 真实的对象可能还不存在, 例如依赖的另外一个模块还没开发完毕

# 6. 好的单元测试
- 简单
    - 防止过度的 setUp, 否则不知道测试用例的错误, 还是业务逻辑的错误
- 隔离
- 可重复
    - 防止在一台机器上可以运行, 另一台机器上无法运行
    - 防止今天成功, 明天失败
- 运行快
    - 防止长时间运行
- 代码覆盖面广
    - 防止测试通过, 但涉及 case 不足