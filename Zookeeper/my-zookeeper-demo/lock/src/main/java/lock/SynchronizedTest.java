package lock;

import com.sun.xml.internal.ws.encoding.TagInfoset;

import java.util.concurrent.TimeUnit;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
public class SynchronizedTest {

    public static void main(String[] args) throws InterruptedException {
        Lock lock = new Lock();

        new Thread(()->{
            System.out.println("线程1准备进入同步块");
            synchronized (lock){
                System.out.println("线程1进入同步块");
                try {
                    lock.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("线程1准备退出同步块");
            }
        }).start();
        Thread.sleep(1000);

        new Thread(()->{
            System.out.println("线程2准备进入同步块");
            synchronized (lock){
                System.out.println("线程2进入同步块");
                try {
                    lock.notify();
                    System.out.println("已通知线程1苏醒");
                    TimeUnit.SECONDS.sleep(5);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("线程2准备退出同步块");
            }
        }).start();
    }
}
