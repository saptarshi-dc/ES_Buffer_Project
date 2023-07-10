package com.saptarshi.internshipproject.configuration;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

import java.time.Duration;
import java.util.concurrent.locks.ReentrantLock;

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
                .withSocketTimeout(Duration.ofSeconds(60))
                .build();

        return RestClients.create(config).rest();
    }
    @Bean
    public ReentrantLock lock(){
        return new ReentrantLock();
    }
}
