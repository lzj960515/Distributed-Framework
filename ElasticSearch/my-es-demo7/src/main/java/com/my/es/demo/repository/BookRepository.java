package com.my.es.demo.repository;

import com.my.es.demo.domain.Book;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * 使用queryMethod方式对es进行crud
 * 参考文档：
 * https://docs.spring.io/spring-data/elasticsearch/docs/4.2.5/reference/html/#elasticsearch.query-methods
 * @author Zijian Liao
 * @since 1.0.0
 */
public interface BookRepository extends Repository<Book, String> {

    List<Book> findByName(String name);

    List<Book> findByNameAndPrice(String name, Integer price);

    List<Book> findByNameOrPrice(String name, Integer price);
}
