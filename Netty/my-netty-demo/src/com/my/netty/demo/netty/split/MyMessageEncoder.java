package com.my.netty.demo.netty.split;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;

/**
 * 处理粘包拆包
 *
 * @author Zijian Liao
 * @since 1.0
 */
public class MyMessageEncoder extends MessageToByteEncoder<String> {

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        System.out.println("call MyMessageEncoder.encode");
        byte[] bytes = msg.getBytes(CharsetUtil.UTF_8);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
