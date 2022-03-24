package com.my.es.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.my.es.domain.ReadBookPd;
import com.my.es.mapper.ReadBookPdMapper;
import com.my.es.repository.ReadBookPdRepository;
import com.my.es.service.IReadBookPdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@Service
public class ReadBookPdServiceImpl extends ServiceImpl<ReadBookPdMapper, ReadBookPd> implements IReadBookPdService {

    @Autowired
    private ReadBookPdRepository readBookPdRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;
    @Override
    public int importAll() {
        List<ReadBookPd> list = super.list();
        Iterable<ReadBookPd> readBookPds = readBookPdRepository.saveAll(list);
        Iterator<ReadBookPd> iterator = readBookPds.iterator();
        int result = 0;
        while (iterator.hasNext()) {
            result++;
            iterator.next();
        }
        return result;
    }

    @Override
    public void findAll(){
        Pageable p = PageRequest.of(1, 2);
        Page<ReadBookPd> all = readBookPdRepository.findAll(p);
        all.forEach(System.out::println);
    }


}
