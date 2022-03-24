package com.my.es.demo.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

/**
 * 用于测试crud
 * @author Zijian Liao
 * @since 1.0.0
 */
@Data
@Document(indexName = "book")
public class Book implements Serializable {

    private static final long serialVersionUID = 8330561837446760834L;

    @Id
    private String id;

//    @Field(type = FieldType.Text)
    private String name;

//    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Integer)
    private Integer price;
}
