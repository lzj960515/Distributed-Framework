package com.my.es.demo.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.my.es.demo.EsDemoApplication;
import com.my.es.demo.domain.Book;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.StringQuery;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 复杂的查询方式
 * @author Zijian Liao
 * @since 1.0.0
 */
@Slf4j
@SpringBootTest(classes = EsDemoApplication.class)
public class ComplexQueryTest {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private RestHighLevelClient restHighLevelClient;
    @Resource
    private RestClient restClient;

    @Test
    public void multiMatch(){
        StringQuery stringQuery = new StringQuery("{\n" +
                "    \"multi_match\": {\n" +
                "      \"query\": \"熊猫\",\n" +
                "      \"fields\": []\n" +
                "    }\n" +
                "  }");
        final SearchHits<Book> search = elasticsearchRestTemplate.search(stringQuery, Book.class);
        final List<SearchHit<Book>> searchHits = search.getSearchHits();
        searchHits.forEach(hit -> {
            System.out.println(hit.getContent());
        });
    }

    @Test
    public void list() throws IOException {
        List<String> fields = new ArrayList<>();
        fields.add("name");
        fields.add("content");
        int current = 0;
        int size = 10;
        while (true){
            final JSONArray array = list("book", fields, current, size);
            array.forEach(System.out::println);
            if(array.size()==0){
                break;
            }
            current++;
        }

    }

    private JSONArray list(String indexName, List<String> fields, Integer current, Integer size) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        final MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        searchSourceBuilder.fetchSource(fields.toArray(new String[0]), null)
                .query(matchAllQueryBuilder)
                .from(current * size)
                .size(size);
        searchRequest.source(searchSourceBuilder);
        final SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        final org.elasticsearch.search.SearchHit[] hits = searchResponse.getHits().getHits();
        JSONArray array = new JSONArray(hits.length);
        for (org.elasticsearch.search.SearchHit hit : hits) {
            array.add(JSON.parseObject(hit.getSourceAsString()));
        }
        return array;
    }

    @Test
    public void restMultiMatch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("book");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        final WrapperQueryBuilder wrapperQueryBuilder = QueryBuilders.wrapperQuery("{\n" +
                "    \"multi_match\": {\n" +
                "      \"query\": \"熊猫\",\n" +
                "      \"fields\": []\n" +
                "    }\n" +
                "  }");
        searchSourceBuilder.query(wrapperQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        final SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        final org.elasticsearch.search.SearchHit[] hits = searchResponse.getHits().getHits();
        for (org.elasticsearch.search.SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }

    }

    @Test
    public void rawSearch() throws IOException {
        String str = "{\n" +
                "  \"size\": 10,\n" +
                "  \"aggs\": {\n" +
                "    \"colors\": {\n" +
                "      \"terms\": {\n" +
                "        \"field\": \"color.keyword\",\n" +
                "        \"size\": 10\n" +
                "      },\n" +
                "      \"aggs\": {\n" +
                "        \"price\": {\n" +
                "          \"filter\": {\n" +
                "            \"bool\": {\n" +
                "              \"must\": {\n" +
                "                \"term\": {\n" +
                "                  \"price\": \"25000\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"aggs\": {\n" +
                "            \"price1\": {\n" +
                "              \"filter\": {\n" +
                "                \"bool\": {\n" +
                "                  \"must\": {\n" +
                "                    \"term\": {\n" +
                "                      \"price\": \"25000\"\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              \"aggs\": {\n" +
                "                \"avg_price\": {\n" +
                "                  \"avg\": {\n" +
                "                    \"field\": \"price\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        Request request = new Request("GET","/cars/_search");
        request.setJsonEntity(str);

        Response response = restClient.performRequest(request);

        String jsonResponse = EntityUtils.toString(response.getEntity());
        log.info("结果:{}",jsonResponse);
    }

    @Test
    public void testFindName(){
        final NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("name", "小熊")).build();
        final SearchHits<Book> result = elasticsearchRestTemplate.search(query, Book.class);
        final List<SearchHit<Book>> searchHits = result.getSearchHits();
        searchHits.forEach(hit -> {
            System.out.println(hit.getContent());
        });
    }

    @Test
    public void testFilter(){
        NativeSearchQuery query = new NativeSearchQueryBuilder().build();
        TermsAggregationBuilder colors = AggregationBuilders.terms("colors").field("color.keyword");
        final FilterAggregationBuilder filter = AggregationBuilders.filter("price", QueryBuilders.boolQuery().must(QueryBuilders.termQuery("price", 25000)));
        final FilterAggregationBuilder filter1 = AggregationBuilders.filter("price1", QueryBuilders.boolQuery().must(QueryBuilders.termQuery("price", 25000)));
        final AvgAggregationBuilder avg = AggregationBuilders.avg("avg_price").field("price");
        filter1.subAggregation(avg);
        filter.subAggregation(filter1);
        colors.subAggregation(filter);
        query.addAggregation(colors);
        final SearchHits<JSONObject> cars = elasticsearchRestTemplate.search(query, JSONObject.class, IndexCoordinates.of("cars"));
        final Aggregations aggregations = cars.getAggregations();
        log.info("结果：{}", aggregations.asMap());
        final ParsedStringTerms colorsResult = aggregations.get("colors");
        final List<? extends Terms.Bucket> buckets = colorsResult.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            log.info("key:{}", bucket.getKeyAsString());
            final Aggregations aggregations1 = bucket.getAggregations();
            Filter filter2 = aggregations1.get("price");
            final Aggregations aggregations2 = filter2.getAggregations();
            final Filter filter3 = aggregations2.get("price1");
            final Aggregations aggregations3 = filter3.getAggregations();
            final Avg avg_price = aggregations3.get("avg_price");
            log.info("avg_price:{}", avg_price.getValue());
        }
    }
}
