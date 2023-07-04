package com.saptarshi.internshipproject.service;

import com.mongodb.client.MongoClient;
import com.saptarshi.internshipproject.model.Batch;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static java.lang.System.exit;

@Component
public class Shutdown {
    @Value("${buffer.type}")
    private String bufferType;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private DefaultKafkaProducerFactory<String,Batch> producerFactory;
    public void stopApplication()
    {
        try {
            if(bufferType.equals("mongodb")) {
                MongoClient mongoClient = applicationContext.getBean(MongoClient.class);
                mongoClient.close();
            }
            else {
                KafkaTemplate kafkaTemplate = applicationContext.getBean(KafkaTemplate.class);
                kafkaTemplate.getProducerFactory().reset();
                kafkaTemplate.destroy();
            }
            RestHighLevelClient restClient = applicationContext.getBean(RestHighLevelClient.class);
            restClient.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
        SpringApplication.exit(applicationContext);
        exit(0);
    }
}
