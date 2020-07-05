package com.my.netty.demo.netty.chat;

import java.io.Serializable;

/**
 * 包装消息
 *
 * @author Zijian Liao
 * @since 1.0
 */
public class WrapMessage implements Serializable {

    private static final long serialVersionUID = 3165017226845753050L;

    /**
     * 1.注册 2.群发 3.私聊
     */
    private int type;

    private String username;

    private String message;

    public WrapMessage(){}

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "WrapMessage{" +
                "type=" + type +
                ", message='" + message + '\'' +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
