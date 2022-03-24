package com.my.es;

import com.my.es.domain.ReadBookPd;
import com.my.es.repository.ReadBookPdRepository;
import com.my.es.service.IReadBookPdService;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
//import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 *
 * 参考博客 https://www.cnblogs.com/powerwu/articles/12047619.html
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EsApplication.class)
public class EsTest {

    @Autowired
    private IReadBookPdService readBookPdService;

    @Autowired
    private ReadBookPdRepository readBookPdRepository;

    @Test
    public void importAll(){
        readBookPdService.importAll();
    }


    @Test
    public void findAll(){
        readBookPdService.findAll();
    }

    @Test
    public void findByPriceBetween(){
        List<ReadBookPd> result = readBookPdRepository.findByPriceBetween(100, 200);
        result.forEach(System.out::println);
    }

    @Test
    public void matchQueryTest(){
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本的分词查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("name", "童话故事大全"));
        // 搜索
        Page<ReadBookPd> result = readBookPdRepository.search(queryBuilder.build());
        System.out.println("总数：" + result.getTotalElements());
        result.forEach(System.out::println);
    }

    @Test
    public void termQueryTest(){
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 去除description字段, null表示不去除任何字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(null, new String[]{"description"}));
        queryBuilder.withQuery(QueryBuilders.termQuery("price",100));
        Page<ReadBookPd> result = readBookPdRepository.search(queryBuilder.build());
        System.out.println("总数：" + result.getTotalElements());
        result.forEach(System.out::println);
    }

    @Test
    public void pageAndOrderTest(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withPageable(PageRequest.of(0,2));
        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));
        Page<ReadBookPd> result = readBookPdRepository.search(queryBuilder.build());
        // 总条数
        long total = result.getTotalElements();
        System.out.println("总条数 = " + total);
        // 总页数
        System.out.println("总页数 = " + result.getTotalPages());
        // 当前页
        System.out.println("当前页：" + result.getNumber());
        // 每页大小
        System.out.println("每页大小：" + result.getSize());
        result.forEach(System.out::println);
    }

    /**
     * 布尔查询
     */
    @Test
    public void boolQueryTest(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("name","安徒生"))
                .must(QueryBuilders.matchQuery("description","丑小鸭"))
        );
        Page<ReadBookPd> result = readBookPdRepository.search(queryBuilder.build());
        System.out.println("总数：" + result.getTotalElements());
        result.forEach(System.out::println);
    }

    /**
     * 模糊查询
     */
    @Test
    public void fuzzyQueryTest(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withPageable(PageRequest.of(0,2));
        queryBuilder.withQuery(QueryBuilders.fuzzyQuery("name","安"));
        Page<ReadBookPd> result = readBookPdRepository.search(queryBuilder.build());
        System.out.println("总数：" + result.getTotalElements());
        result.forEach(System.out::println);
    }

    @Test
    public void aggTest(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 不查询任何结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
        // 1、添加一个新的聚合，聚合类型为terms，聚合名称为commentNum，聚合字段为commentNum
        queryBuilder.addAggregation(
                AggregationBuilders.terms("commentNum").field("commentNum"));
        // 2、查询,需要把结果强转为AggregatedPage类型
        AggregatedPage<ReadBookPd> aggPage = (AggregatedPage<ReadBookPd>) readBookPdRepository.search(queryBuilder.build());
        // 3、解析
        // 3.1、从结果中取出名为commentNum的那个聚合，
        // 因为是利用int类型字段来进行的term聚合，所以结果要强转为LongTerms类型
        LongTerms agg = (LongTerms)aggPage.getAggregation("commentNum");
        // 3.2、获取桶
        List<LongTerms.Bucket> buckets = agg.getBuckets();
        // 3.3、遍历
        for (LongTerms.Bucket bucket : buckets) {
            // 3.4、获取桶中的key
            System.out.println(bucket.getKeyAsString());
            // 3.5、获取桶中的文档数量
            System.out.println(bucket.getDocCount());
        }

    }

    /**
     * 嵌套聚合，求平均值
     */
    @Test
    public void testSubAgg(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 不查询任何结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
        // 1、添加一个新的聚合，聚合类型为terms，聚合名称为brands，聚合字段为brand
        queryBuilder.addAggregation(
                AggregationBuilders.terms("commentNum").field("commentNum")
                        .subAggregation(AggregationBuilders.avg("priceAvg").field("price")) // 在评论聚合桶内进行嵌套聚合，求平均值
        );
        // 2、查询,需要把结果强转为AggregatedPage类型
        AggregatedPage<ReadBookPd> aggPage = (AggregatedPage<ReadBookPd>) readBookPdRepository.search(queryBuilder.build());
        // 3、解析
        // 3.1、从结果中取出名为commentNum的那个聚合，
        // 因为是利用int类型字段来进行的term聚合，所以结果要强转为LongTerms类型
        LongTerms agg = (LongTerms) aggPage.getAggregation("commentNum");
        // 3.2、获取桶
        List<LongTerms.Bucket> buckets = agg.getBuckets();
        // 3.3、遍历
        for (LongTerms.Bucket bucket : buckets) {
            // 3.4、获取桶中的key  3.5、获取桶中的文档数量
            System.out.println(bucket.getKeyAsString() + " " + bucket.getDocCount());

            // 3.6.获取子聚合结果：
//            InternalAvg avg = (InternalAvg) bucket.getAggregations().asMap().get("priceAvg");
//            System.out.println("平均售价：" + avg.getValue());
        }

    }
}
