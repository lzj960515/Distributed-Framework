package com.my.es;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Zijian Liao
 * @since
 */
public class AuthTest {

    @Test
    public void test() throws IOException {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "kqinfo@elastic*2021"));
        RestClientBuilder restClientBuilder = RestClient.builder(
                new HttpHost("122.9.35.11", 19200, "http")
                , new HttpHost("122.9.35.11", 19300, "http")
                , new HttpHost("122.9.35.11", 19400, "http"))
                .setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClientBuilder);
        final SearchRequest getRequest = new SearchRequest("test");
        final SearchResponse getResponse = restHighLevelClient.search(getRequest, RequestOptions.DEFAULT);
        for (SearchHit hit : getResponse.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }
    }
}
