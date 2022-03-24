package com.my.netty.demo.netty.split;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 处理粘包拆包
 *
 * @author Zijian Liao
 * @since 1.9
 */
public class MyMessageDecoder extends ByteToMessageDecoder {

    int length = 0;
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("call MyMessageDecoder.decode");
        //先判断读的数据是否大于4,读出数据长度
        if (in.readableBytes() < 4) return;
        if (length == 0){
            length = in.readInt();
        }
        if (in.readableBytes() < length) {
            System.err.println("长度不够，继续等待");
            return;
        }
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        out.add(new String(bytes));
        length = 0;
    }
}
