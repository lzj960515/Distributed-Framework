package com.my.es.demo.test;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.my.es.demo.EsDemoApplication;
import com.my.es.demo.domain.Book;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@Slf4j
@SpringBootTest(classes = EsDemoApplication.class)
public class GetDataTest {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    private static final String url = "http://api.tianapi.com/txapi/story/index?key=32b536b28875435c78e0230eb72ef293&num=10&type=3";
    AtomicInteger atomicInteger = new AtomicInteger(1);
    Random random = new Random();

    @Test
    public void getBook(){
        List<Book> saveList = new ArrayList<>(1000);
        for (int i = 1; i <= 40; i++) {
            final JSONArray bookList = getBookList(i);
            if(bookList!=null){
                bookList.forEach(o -> {
                    JSONObject bookJson = (JSONObject) o;
                    Book book = new Book();
                    book.setId(String.valueOf(atomicInteger.getAndIncrement()));
                    book.setName(bookJson.getStr("title"));
                    book.setContent(bookJson.getStr("content"));
                    book.setPrice(random.nextInt(100));
                    saveList.add(book);
                });
            }
        }
        elasticsearchRestTemplate.save(saveList);
    }

    private JSONArray getBookList(Integer page){
        String getUrl = url + "&page="+page;
        final String s = HttpUtil.get(getUrl);
        final JSONObject jsonObject = JSONUtil.parseObj(s);
        return jsonObject.getJSONArray("newslist");
    }
}
