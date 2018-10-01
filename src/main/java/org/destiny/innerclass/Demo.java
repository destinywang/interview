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
        String myName = "destiny";
        IHello hello = new IHello() {
            @Override
            public void say(String name) {
                System.out.println("hello " + name);
            }
        };
        hello.say(myName);
    }

    public static void main(String[] args) {
        Demo demo = new Demo();
        demo.run();
    }
}
