package com.my.cannal;

import com.alibaba.otter.canal.client.CanalConnector;

/**
 * @author Zijian Liao
 * @since
 */
public class CanalTest {

    public static void main(String[] args) {
        CanalConnector connector = CanalConnectorFactory.connector();
        new CanalDataHandler(connector).start();
    }
}
