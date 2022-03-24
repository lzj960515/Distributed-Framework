package com.my.es.demo.domain;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@Data
@Document(indexName = "cars")
public class Car {

    private Integer price;

    private String color;

    private String make;

    private String sold;
}
