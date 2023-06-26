package com.saptarshi.internshipproject.configuration;

import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan(basePackages = {"com.saptarshi.internshipproject"})
public class Config extends AbstractElasticsearchConfiguration {
    @Value("${elasticsearch.url}")
    public String elasticsearchUrl;

    @Bean
    @Override
    public RestHighLevelClient elasticsearchClient(){
        final ClientConfiguration config=ClientConfiguration.builder()
                .connectedTo(elasticsearchUrl)
                .build();
        return RestClients.create(config).rest();
    }
}
//@Configuration
//@ComponentScan(basePackages = {"com.saptarshi.internshipproject"})
//public class Config {
//    @Value("${mongodb.uri}")
//    static String connectionString;
//    public static void main(String[] args) {
//        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
//            List<Document> databases = mongoClient.listDatabases().into(new ArrayList<>());
//            databases.forEach(db -> System.out.println(db.toJson()));
//        }
//    }
//}

//@Configuration
//@EnableElasticsearchRepositories(basePackages="com.saptarshi.internshipproject.repository")
//public class Config {
//    @Bean
//    public ElasticsearchClient elasticsearchClient() {
//
//        RestClient httpClient = RestClient.builder(new HttpHost("localhost", 9200))
//                .build();
//
//        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper();
//
//        ElasticsearchTransport transport = new RestClientTransport(httpClient, jsonpMapper);
//        ElasticsearchClient client = new ElasticsearchClient(transport);
//        return client;
//    }
//}
