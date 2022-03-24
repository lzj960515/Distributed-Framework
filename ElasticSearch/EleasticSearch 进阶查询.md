# EleasticSearch 进阶查询

- 多字段或语句查询

  ```http
  GET /book/_validate/query?explain
  {
    "query": {
      "multi_match": {
        "query": "童话故事大全",
        "fields": ["name", "description"]
      }
    }
  }
  ```

- 解释模式

  ```http
  GET /book/_validate/query?explain
  {
    "query": {
      "multi_match": {
        "query": "童话故事大全",
        "fields": ["name", "description"]
      }
    }
  }
  ```

- match

  ```java
  GET /book/_search
  {
    "query": {
      "match": {
        "name": "童话故事大全"
      }
    }
  }
  ```

  - 对分词or或者and查询

  ```http
  GET /book/_search
  {
    "query": {
      "match": {
        "name": {
          "query": "童话故事大全",
          "operator": "and"
        }
      }
    }
  }
  ```

- term

  ```http
  GET /book/_search
  {
    "query": {
      "term": {
        "name": {
          "value": "中华故事"
        }
      }
    }
  }
  ```

  > 不会对name进行分词，直接使用`中华故事`进行作关键字查询

  ```http
  GET /book/_search
  {
    "query": {
      "terms": {
        "name": [
          "中华",
          "故事"
        ]
      }
    }
  }
  ```

  > 有`中华`和`故事`其中之一即可

- 最小匹配查询

  ```properties
  #分析一下看下分了几个词
  GET /book/_analyze
  {
    "field": "name",
    "text": "故事大全"
  }
  #最小匹配查询
  GET /book/_search
  {
    "query": {
      "match": {
        "name": {
          "query": "故事大全",
          #最少匹配两个词
          "minimum_should_match": 2
        }
      }
    }
  }
  ```

- 多字段添加权重

  ```http
  GET /book/_search
  {
    "query": {
      "multi_match": {
        "query": "花朵",
        "fields": ["name^10","description"]
      }
    }
  }
  ```

- 权重平滑处理，更加突出权重，最大值加上其他值的0.3倍

  ```http
  GET /book/_search
  {
    "query": {
      "multi_match": {
        "query": "花朵",
        "fields": ["name^10","description"],
        "tie_breaker": 0.3
      }
    }
  }
  
  ```

- 取最好的字段

  ```http
  GET /book/_search
  {
    "query": {
      "multi_match": {
        "query": "大自然的旅行故事",
        "fields": ["name","description"],
        "type": "best_fields"
      }
    }
  }
  ```

- 多字段分值相加

  ```http
  GET /book/_search
  {
    "query": {
      "multi_match": {
        "query": "大自然的旅行故事",
        "fields": ["name","description"],
        "type": "most_fields"
      }
    }
  }
  ```

- query_string

  ```http
  GET /book/_search
  {
    "query": {
      "query_string": {
        "default_field": "name",
        "query": "大自然 AND 旅行"
      }
    }
  }
  ```

  > 可使用 AND OR NOT

- bool查询

  - should查询，其中有一个条件为true即可，但true越多的排在越前面

  ```http
  GET /book/_search
  {
    "query": {
      "bool": {
        "should": [
          {
            "match": {
              "name": "安徒生"
            }
          },
          {
            "match": {
              "description": "丑小鸭"
            }
          }
        ]
      }
    }
  }
  ```

  - must查询，必须全部为true

  ```http
  GET /book/_search
  {
    "query": {
      "bool": {
        "must": [
          {
            "match": {
              "name": "安徒生"
            }
          },
          {
            "match": {
              "description": "丑小鸭"
            }
          }
        ]
      }
    }
  }
  ```

  - must_not，必须全部为false

  ```http
  GET /book/_search
  {
    "query": {
      "bool": {
        "must_not": [
          {
            "match": {
              "name": "安徒生"
            }
          },
          {
            "match": {
              "description": "丑小鸭"
            }
          }
        ]
      }
    }
  }
  ```

- filter

  ```http
  GET /book/_search
  {
    "query": {
      "bool": {
        "filter": [
          {
            "range": {
              "price": {
                "lte": 2000,
                "gte": 1
              }
            }
          },
          {
            "match": {
              "name":"故事"
            }
          }
        ]
      }
    } ,
    "sort": [
      {
        "commentNum": {
          "order": "desc"
        }
      }
    ]
  }
  ```

- 同义词查询

  - 在每个es的`config/analysis-ik`新建文件夹`synonyms.txt`

  - 编辑以下内容

    ```shell
    苹果,iphone,apple
    美丽,漂亮,气质好
    ```

  - 建立索引

    ```http
    PUT /tests
    {
      "settings": {
        "number_of_replicas": 1,
        "number_of_shards": 1,
        "analysis": {
          "filter": {
            "my_synonym_filter": {
              "type": "synonym",
              "synonyms_path": "analysis-ik/synonyms.txt"
            }
          },
          "analyzer": {
            "ik_syno": {
              "type": "custom",
              "tokenizer": "ik_smart",
              "filter": [
                "my_synonym_filter"
              ]
            },
            "ik_syno_max": {
              "type": "custom",
              "tokenizer": "ik_max_word",
              "filter": [
                "my_synonym_filter"
              ]
            }
          }
        }
      },
      "mappings": {
        "_doc": {
          "properties": {
            "name": {
              "type": "text",
              "analyzer": "ik_syno_max",
              "search_analyzer": "ik_syno"
            }
          }
        }
      }
    }
    ```

  - 插入两条数据

    ```http
    POST /tests/_doc/1/_create
    {
      "name":"苹果"
    }
    
    POST /tests/_doc/2/_create
    {
      "name":"apple"
    }
    ```

  - 尝试同义词查询

    ```http
    GET /tests/_search
    {
      "query": {
        "match": {
          "name": "iphone"
        }
      }
    }
    ```

  - 查询结果

    ```http
    {
      "took" : 38,
      "timed_out" : false,
      "_shards" : {
        "total" : 1,
        "successful" : 1,
        "skipped" : 0,
        "failed" : 0
      },
      "hits" : {
        "total" : 2,
        "max_score" : 0.33425623,
        "hits" : [
          {
            "_index" : "tests",
            "_type" : "_doc",
            "_id" : "1",
            "_score" : 0.33425623,
            "_source" : {
              "name" : "苹果"
            }
          },
          {
            "_index" : "tests",
            "_type" : "_doc",
            "_id" : "2",
            "_score" : 0.33425623,
            "_source" : {
              "name" : "apple"
            }
          }
        ]
      }
    }
    ```

    