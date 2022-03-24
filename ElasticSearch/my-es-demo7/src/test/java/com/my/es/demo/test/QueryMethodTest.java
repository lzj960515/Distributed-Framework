package com.my.es.demo.test;

import com.my.es.demo.EsDemoApplication;
import com.my.es.demo.domain.Book;
import com.my.es.demo.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactory;

import javax.annotation.Resource;
import java.util.List;

/**
 * 使用queryMethod方式进行数据查询
 * @author Zijian Liao
 * @since 1.0.0
 */
@Slf4j
@SpringBootTest(classes = EsDemoApplication.class)
public class QueryMethodTest {

    @Resource
    private BookRepository bookRepository;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 查询时会将分词结果and连接
     * GET /book/_search
     * {
     *   "query": {
     *     "bool": {
     *       "must": [
     *         {
     *           "query_string": {
     *             "query": "小熊",
     *             "fields": [
     *               "name"
     *             ],
     *             "default_operator": "and"
     *           }
     *         }
     *       ]
     *     }
     *   }
     * }
     */
    @Test
    public void testFind(){
        final List<Book> bookList = bookRepository.findByName("小熊");
        log.info(bookList.toString());
        MatcherAssert.assertThat(bookList, CoreMatchers.notNullValue());
        // 实现方式同下 testCount()
    }

    @Test
    public void testCount(){
        Criteria criteria = new Criteria("name").is("小熊");
        Query query = new CriteriaQuery(criteria);
        final long count = elasticsearchRestTemplate.count(query, Book.class);
        log.info(count + "");
    }

    @Test
    public void testFindAnd(){
        final List<Book> bookList = bookRepository.findByNameAndPrice("小熊", 91);
        log.info(bookList.toString());
        MatcherAssert.assertThat(bookList, CoreMatchers.notNullValue());
    }

    @Test
    public void testFindOr(){
        final List<Book> bookList = bookRepository.findByNameOrPrice("小熊", 84);
        log.info(bookList.toString());
        MatcherAssert.assertThat(bookList, CoreMatchers.notNullValue());
    }

    /**
     * 尝试自生成代理类
     */
    @Test
    public void testGetRepository(){
        final ElasticsearchRepositoryFactory elasticsearchRepositoryFactory = new ElasticsearchRepositoryFactory(elasticsearchRestTemplate);
        // 如何产生class?
        final BookRepository repository = elasticsearchRepositoryFactory.getRepository(BookRepository.class);
        final List<Book> bookList = repository.findByNameOrPrice("小熊", 84);
        log.info(bookList.toString());
        MatcherAssert.assertThat(bookList, CoreMatchers.notNullValue());
    }
}
