package com.my.es.repository;

import com.my.es.domain.ReadBookPd;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
public interface ReadBookPdRepository extends ElasticsearchRepository<ReadBookPd, Long> {

    List<ReadBookPd> findByPriceBetween(Integer lowPrice, Integer highPrice);
}
