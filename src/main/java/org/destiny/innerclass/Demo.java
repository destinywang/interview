package org.destiny.innerclass;

/**
 * @author destiny
 * destinywk@163.com
 * ------------------------------------------------------------------
 * <p></p>
 * ------------------------------------------------------------------
 * design by 2018/10/1 22:49
 * @version 1.8
 * @since JDK 1.8.0_101
 */
public class Demo {

    public void run() {
        final String myName = "destiny";
        IHello hello = new IHello() {
            @Override
            public void say() {
                System.out.println("hello " + myName);
            }
        };
        hello.say();
    }

    public static void main(String[] args) {
        Demo demo = new Demo();
        demo.run();
    }
}
