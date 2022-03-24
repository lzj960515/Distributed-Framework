package com.my.cannal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;

import java.net.InetSocketAddress;

/**
 * Canal连接工厂
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class CanalConnectorFactory {

    public static CanalConnector connector(){
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress("8.129.54.157", 11111),
                "example",
                "canal",
                "canal");
        connector.connect();
        // 订阅时填入filter将会覆盖服务端配置的过滤表达式
        //connector.subscribe("test.fruit");
        connector.subscribe();
        // 回滚到未进行ack的地方，下次fetch的时候，可以从最后一个没有ack的地方开始拉取数据
        connector.rollback();
        return connector;
    }
}
