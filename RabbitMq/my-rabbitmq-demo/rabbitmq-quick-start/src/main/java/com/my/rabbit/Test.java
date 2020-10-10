package com.my.rabbit;

/**
 * @author Zijian Liao
 * @since
 */
public class Test {

    public static Object object = new Object();

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            new Thread(()->{
                System.out.println(Thread.currentThread().getName() + "准备获取锁");
                synchronized (object){
                    System.err.println(Thread.currentThread().getName() + "获取到锁");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.err.println(Thread.currentThread().getName() + "释放锁");
                }
            },"thread-"+i).start();
        }
    }
}
