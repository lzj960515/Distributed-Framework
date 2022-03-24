package com.my.es.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "read_book_pd")
@Document(indexName = "book", type = "readbook",shards = 1,replicas = 1)
public class ReadBookPd implements Serializable {

    public ReadBookPd(){}

    private static final long serialVersionUID = -4322834406547757077L;
    @Id
    private Long id;
    @Field(analyzer = "ik_max_word", searchAnalyzer = "ik_smart", type = FieldType.Text)
    private String name;
    @Field(analyzer = "english", type = FieldType.Text)
    private String enName;
    @Field(type = FieldType.Keyword)
    private String author;
    @Field(index = false, type = FieldType.Keyword)
    private String imgurl;
    @Field(analyzer = "ik_max_word", searchAnalyzer = "ik_smart", type = FieldType.Text)
    private String description;
    @Field(analyzer = "ik_max_word", searchAnalyzer = "ik_smart", type = FieldType.Text)
    private String category;

    private Integer creator;

    private Integer status;

    private Integer commentNum;

    private Integer price;
/*    @Field(type = FieldType.Date)
    private Date createTime;
    @Field(type = FieldType.Date)
    private Date updateTime;*/
}
