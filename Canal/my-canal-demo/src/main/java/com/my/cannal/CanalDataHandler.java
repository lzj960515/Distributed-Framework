package com.my.cannal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Canal 数据处理
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class CanalDataHandler {

    private static final int MAX_SIZE = 1024;

    private final CanalConnector connector;

    public CanalDataHandler(CanalConnector canalConnector){
        this.connector = canalConnector;
    }

    public void start(){
        new Thread(this::process).start();
    }

    public void process(){
        for(;;){
            Message message = connector.getWithoutAck(MAX_SIZE, 2L, TimeUnit.SECONDS);
            long id = message.getId();
            List<CanalEntry.Entry> entries = message.getEntries();
            if(id != -1 && entries.size() > 0){
                handleMessage(entries);
            }
            connector.ack(id);
        }
    }

    private void handleMessage(List<CanalEntry.Entry> entries){
        entries.forEach(entry -> {
            if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                // 数据库库名
                String schemaName = entry.getHeader().getSchemaName();
                String tableName = entry.getHeader().getTableName();
                CanalEntry.RowChange rowChage = null;
                try {
                    rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                } catch (Exception e) {
                    throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(), e);
                }
                CanalEntry.EventType eventType = rowChage.getEventType();
                System.out.printf("监听到更新，数据库：%s, 表：%s, 事件类型：%s %n", schemaName, tableName, eventType);
                rowChage.getRowDatasList().forEach(rowData -> {
                    List<CanalEntry.Column> columnList;
                    if(eventType == CanalEntry.EventType.DELETE){
                        columnList = rowData.getBeforeColumnsList();
                    }else {
                        columnList = rowData.getAfterColumnsList();
                    }
                    System.out.println(buildSql(eventType, columnList, schemaName, tableName));
                });

            }
        });
    }

    private String buildSql(CanalEntry.EventType eventType, List<CanalEntry.Column> columnList, String schemaName, String tableName){
        if(eventType == CanalEntry.EventType.INSERT){
            return buildInsertSql(columnList, schemaName, tableName);
        }
        if(eventType == CanalEntry.EventType.UPDATE){
            return "update";
        }
        if(eventType == CanalEntry.EventType.DELETE){
            return "delete";
        }
        throw new UnsupportedOperationException("不支持的sql方式");
    }

    private String buildInsertSql(List<CanalEntry.Column> columnList, String schemaName, String tableName){
        // insert into test.fruit (id, name, price, create_time) values (1, 'apple', 3, '2020-10-10 10:10:10')
        // insert into
        StringBuilder sb = new StringBuilder("insert into ");
        // test.fruit (
        sb.append(schemaName).append(".").append(tableName).append(" (");
        StringBuilder values = new StringBuilder("values (");
        columnList.forEach(column -> {
            // id, name, price, create_time
            sb.append(column.getName()).append(",");
            // 1, 'apple', 3, '2020-10-10 10:10:10'
            values.append("'").append(column.getValue()).append("'").append(",");
        });
        sb.delete(sb.length() - 1, sb.length());
        sb.append(")");
        values.delete(values.length() - 1, values.length());
        values.append(")");
        sb.append(" ").append(values);
        return sb.toString();
    }
}
