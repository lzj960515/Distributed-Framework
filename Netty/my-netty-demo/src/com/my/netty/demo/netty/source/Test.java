package com.my.netty.demo.netty.source;

/**
 * 测试源码细节
 *
 * @author Zijian Liao
 * @since 1.0
 */
public class Test {
    public static void main(String[] args) {

    }
    public Woman convert(Person person){
        return (Woman) person;
    }



    protected  void test(){
        Person person =  new Man();
        convert(person);
    }

    public void test2(){
        test();
    }
}
