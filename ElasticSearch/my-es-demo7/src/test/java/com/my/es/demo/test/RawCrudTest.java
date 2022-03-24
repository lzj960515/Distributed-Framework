package com.my.es.demo.test;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.my.es.demo.EsDemoApplication;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 原生方式
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@Slf4j
@SpringBootTest(classes = EsDemoApplication.class)
public class RawCrudTest {

    @Resource
    private RestClient restClient;
    @Resource
    private RestHighLevelClient restHighLevelClient;

    private static final String UPDATE_PATTERN = "/%s/_update/%s?timeout=1m";

    @SneakyThrows
    public String update(String indexName, String id, JSONObject data){
        final String uri = String.format(UPDATE_PATTERN, indexName, id);
        JSONObject doc = new JSONObject();
        doc.put("doc", data);
        Request request = new Request("POST",uri);
        request.setJsonEntity(doc.toString());
        final Response response = restClient.performRequest(request);
        String result = EntityUtils.toString(response.getEntity());
        final cn.hutool.json.JSONObject resultJson = JSONUtil.parseObj(result);
        return resultJson.getStr("_id");
    }

    @Test
    public void testUpdate(){
        JSONObject data = new JSONObject();
        data.put("bb","cc");
        data.put("a","d");

        System.out.println(this.update("lzj2", "1", data));
    }

    @SneakyThrows
    public JSONObject get(String indexName, String id){
        GetRequest getRequest = new GetRequest(indexName, id);
        final GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        return JSON.parseObject(getResponse.getSourceAsString());
    }

    @Test
    public void testGet(){
        final JSONObject jsonObject = this.get("book", "1");
        System.out.println(jsonObject.toJSONString());
    }

    private static final String SEARCH_PATTERN = "/%s/_search";
    private static final String PAGE_QUERY_PATTERN = "{\"from\":%d,\"size\":%d,\"query\":{\"bool\":{\"must\":[%s]}}}";

    public JSONArray page(String indexName, int current, int size, List<String> dslList){
        final String dsl = String.join(",", dslList);
        final String query = String.format(PAGE_QUERY_PATTERN, (current-1) * size, size, dsl);
        final String result = this.send("GET", String.format(SEARCH_PATTERN, indexName), query);
        final JSONObject resultJson = JSON.parseObject(result);
        final JSONObject hits = resultJson.getJSONObject("hits");
        final JSONArray dataHits = hits.getJSONArray("hits");
        JSONArray sourceArray = new JSONArray(hits.size());
        for (Object hit : dataHits) {
            JSONObject source = (JSONObject) hit;
            sourceArray.add(source.getJSONObject("_source"));
        }
        return sourceArray;
    }

    private static final String SQL_TRANSLATE = "{\"query\":\"%s\"}";

    @Test
    public void testPage(){
        List<String> dslList = new ArrayList<>();
        dslList.add("{\"match\":{\"name\":\"小\"}}");
        dslList.add("{\"match\":{\"price\":92}}");
        dslList.add("{\"range\":{\"price\":{\"gte\":10}}}");
        final JSONArray book = this.page("book", 1, 10, dslList);
        System.out.println(book);
    }


    @Test
    public void testSqlTranslate(){
        String sql = "select * from book where name = '小熊' AND (price >= 10 OR  Match(content,'A'))";
        final JSONObject jsonObject = sqlTranslate(sql);
        log.info(jsonObject.toJSONString());
    }

    public JSONObject sqlTranslate(String sql){
        final String body = String.format(SQL_TRANSLATE, sql);
        final String result = this.send("GET", "/_sql/translate", body);
        return JSON.parseObject(result);
    }

    @SneakyThrows
    private String send(String method, String uri, String body){
        Request request = new Request(method,uri);
        request.setJsonEntity(body);
        final Response response = restClient.performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }


}
