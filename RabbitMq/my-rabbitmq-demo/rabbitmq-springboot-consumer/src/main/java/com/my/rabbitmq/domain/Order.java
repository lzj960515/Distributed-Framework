package com.my.rabbitmq.domain;

/**
 * @author Zijian Liao
 * @since
 */
public class Order {

    private String no;

    private int money;


    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    @Override
    public String toString() {
        return "Order{" +
                "no='" + no + '\'' +
                ", money=" + money +
                '}';
    }
}
