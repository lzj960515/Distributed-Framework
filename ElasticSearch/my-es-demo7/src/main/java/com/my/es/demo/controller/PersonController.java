package com.my.es.demo.controller;

import com.my.es.demo.aop.ParamAspect;
import com.my.es.demo.aop.ParamInsert;
import com.my.es.demo.domain.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@RestController
@RequestMapping("/person")
@RequiredArgsConstructor
public class PersonController {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;


    @PostMapping("/index")
    public String index(){
        final IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(Person.class);
        indexOperations.createWithMapping();
        return "ok";
    }

    @ParamInsert
    @PostMapping("/save")
    public String save(@RequestBody Person person){
        elasticsearchOperations.save(person);
        return "ok";
    }

    @GetMapping("/{id}")
    public Person findById(@PathVariable("id") Long id) {
        return elasticsearchOperations.get(id.toString(), Person.class);
    }
}
