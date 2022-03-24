package demo;

import java.util.concurrent.TimeUnit;

/**
 * main
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("main~");
        for(;;){
            System.out.println("在跑着呢...");
            TimeUnit.SECONDS.sleep(5);
        }
    }
}
