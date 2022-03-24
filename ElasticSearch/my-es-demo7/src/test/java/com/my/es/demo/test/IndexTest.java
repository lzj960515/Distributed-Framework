package com.my.es.demo.test;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.my.es.demo.EsDemoApplication;
import com.my.es.demo.domain.Person;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexInformation;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EsDemoApplication.class)
public class IndexTest {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 创建索引
     */
    @Test
    public void testCreateIndex(){
        final IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(Person.class);
        if(!indexOperations.exists()){
            indexOperations.createWithMapping();
        }
        MatcherAssert.assertThat(indexOperations.exists(), CoreMatchers.is(true));
    }

    @Test
    public void testCreateIndex2(){
        final IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(IndexCoordinates.of("lzj"));
        if(!indexOperations.exists()){
            indexOperations.create();
        }
        MatcherAssert.assertThat(indexOperations.exists(), CoreMatchers.is(true));
    }

    @Test
    public void testIndexExist(){
        final IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(IndexCoordinates.of("lzj"));
        MatcherAssert.assertThat(indexOperations.exists(), CoreMatchers.is(false));
    }

    /**
     * 测试校验文档是否存在
     * 如果索引不存在，则报错
     *
     */
    @Test
    public void testDocExist(){
        final boolean exists = elasticsearchRestTemplate.exists("1", IndexCoordinates.of("lzj"));
        MatcherAssert.assertThat(exists, CoreMatchers.is(false));
    }

    @Test
    public void testDelete(){
        final IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(Person.class);
        MatcherAssert.assertThat(indexOperations.delete(), CoreMatchers.is(true));
    }

    /**
     * 创建索引时不会根据注解定义setting与mapping
     */
    @Test
    public void testIndexAndSave(){
        Person person = new Person();
        person.setId(1L);
        person.setName("赵云");
        person.setDesc("赵云，字子龙，是中国三国时期的蜀汉武将，生于常山真定，身高八尺，姿颜雄伟。初从公孙瓒，后归刘备、刘禅。历任牙门将军，偏将军、领桂阳太守，翊军将军，领中护军、征南将军，封永昌亭侯，镇东将军。箕谷失利后自请贬为镇军将军。故后追谥曰顺平侯，是为永昌亭顺平侯。");
        final IndexQuery indexQuery = new IndexQueryBuilder()
                .withObject(person)
                .build();
        final String documentId = elasticsearchRestTemplate.index(indexQuery, elasticsearchRestTemplate.getIndexCoordinatesFor(Person.class));
        log.info(documentId);
        MatcherAssert.assertThat(documentId, CoreMatchers.notNullValue());
    }

    @Test
    public void testMappingAndSetting(){
        final IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(Person.class);
        log.info(indexOperations.getSettings().toString());
        log.info(indexOperations.getMapping().toString());
    }

    @Test
    public void save() throws IOException {
        String indexName = "lzj3";
        String id = "1";
        JSONObject data  = new JSONObject();
        data.set("a","b");
        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(id)
                .withObject(data)
                .build();
        final String documentId = elasticsearchRestTemplate.index(indexQuery, IndexCoordinates.of(indexName));
        MatcherAssert.assertThat(documentId, CoreMatchers.notNullValue());
        GetRequest getRequest = new GetRequest(indexName, id);
        final GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(JSON.parseObject(getResponse.getSourceAsString()));
    }
}
