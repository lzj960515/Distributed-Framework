package com.my.es.demo.test;

import com.my.es.demo.EsDemoApplication;
import com.my.es.demo.domain.Book;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试简单的crud
 * @author Zijian Liao
 * @since 1.0.0
 */
@Slf4j
@SpringBootTest(classes = EsDemoApplication.class)
public class BasicCrudTest {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 当id为字符串类型时，es将自动生成id
     */
    @Test
    public void testCreate(){
        Book book = new Book();
        book.setId("EWKdtXwB-2K-bFQHHiw7");
        book.setName("安徒生与童话");
        book.setContent("《安徒生童话》是丹麦作家安徒生的童话作品，也是世界上最有名的童话作品集之一。其中最著名的童话故事有《海的女儿》、《小锡兵》、《冰雪女王》、《拇指姑娘》、《卖火柴的小女孩》、《丑小鸭》和《红鞋》等。尽管创作体裁属于童话，但是其中蕴含了丰富的人生哲理。");
        book.setPrice(100);
        final Book save = elasticsearchRestTemplate.save(book);
        MatcherAssert.assertThat(save.getId(), CoreMatchers.notNullValue());
    }

    @Test
    public void testQuery(){
        final Book book = elasticsearchRestTemplate.get("EWKdtXwB-2K-bFQHHiw7", Book.class);
        MatcherAssert.assertThat(book, CoreMatchers.notNullValue());
        log.info(book.toString());
    }

    /**
     * es中，使用相同id保存即为更新
     */
    @Test
    public void testUpdate(){
        Book book = new Book();
        book.setId("EWKdtXwB-2K-bFQHHiw7");
        book.setName("拇指姑娘");
        book.setContent("有一个农妇在她的花园中种了一株大麦。后来大麦长出了花苞，农妇便在花苞上亲吻了一下，许下了想得到一个拇指般小的姑娘的愿望。在花苞盛开出花朵后，农妇发现一个迷你但美丽的小女孩从舒展开的花瓣中出现，十分惊喜。由于小女孩的身体仅像农妇的拇指般大小，因此农妇将小女孩命名为“拇指姑娘”（Thumbelina）。");
        book.setPrice(20);
        final Book save = elasticsearchRestTemplate.save(book);
        log.info(save.toString());
        MatcherAssert.assertThat(save.getId(), CoreMatchers.notNullValue());
    }

    @Test
    public void testDelete(){
        final String id = elasticsearchRestTemplate.delete("EWKdtXwB-2K-bFQHHiw7", Book.class);
        log.info(id);
        MatcherAssert.assertThat(id, CoreMatchers.is("EWKdtXwB-2K-bFQHHiw7"));
    }

    @Test
    public void testDelete2(){
        final String id = elasticsearchRestTemplate.delete("1", IndexCoordinates.of("lzj2"));
        MatcherAssert.assertThat(id, CoreMatchers.is("1"));
    }

    @Test
    public void testBatchCreate(){
        List<Book> bookList = new ArrayList<>();
        final Iterable<Book> saveList = elasticsearchRestTemplate.save(bookList);
        MatcherAssert.assertThat(saveList, CoreMatchers.notNullValue());
    }

}