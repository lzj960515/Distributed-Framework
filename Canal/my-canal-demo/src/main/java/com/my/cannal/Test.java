package com.my.cannal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.alibaba.otter.canal.protocol.Message;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
public class Test {

    public static void main(String args[]) {
        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress("8.129.54.157", 11111),
                "example",
                "canal",
                "canal");
        try {
            connector.connect();
            // 订阅时填入filter将会覆盖服务端配置的过滤表达式
            //connector.subscribe("test.fruit");
            connector.subscribe(".*\\..*");
            // 回滚到未进行ack的地方，下次fetch的时候，可以从最后一个没有ack的地方开始拉取数据
            connector.rollback();
            int totalEmptyCount = 1024;
            // 死循环获取数据
            for (; ; ) {
                // 获取指定数量的数据
                Message message = connector.getWithoutAck(totalEmptyCount);
                long batchId = message.getId();
                List<Entry> entries = message.getEntries();
                if (batchId != -1 && entries.size() > 0) {
                    try {
                        System.out.println("batchId: " + batchId);
                        // 消费
                        printEntry(message.getEntries());
                        // 提交确认
                        connector.ack(batchId);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        connector.rollback();
                    }
                }
            }
        } finally {
            connector.disconnect();
        }
    }

    private static void printEntry(List<Entry> entrys) {
        for (Entry entry : entrys) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }

            RowChange rowChage = null;
            try {
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(), e);
            }

            EventType eventType = rowChage.getEventType();
            System.out.println(String.format("================> binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));
            if(rowChage.getIsDdl()){
                System.out.println("sql ==>" + rowChage.getSql());
            }
            for (RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == EventType.DELETE) {
                    printColumn(rowData.getBeforeColumnsList());
                } else if (eventType == EventType.INSERT) {
                    printColumn(rowData.getAfterColumnsList());
                } else {
                    System.out.println("-------> before");
                    printColumn(rowData.getBeforeColumnsList());
                    System.out.println("-------> after");
                    printColumn(rowData.getAfterColumnsList());
                }
            }
        }
    }

    private static void printColumn(List<Column> columns) {
        for (Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }
}
