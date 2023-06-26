package com.saptarshi.internshipprojectmdb.service;

import com.mongodb.client.MongoClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import static java.lang.System.exit;

@Component
public class Shutdown {
    @Autowired
    private ApplicationContext applicationContext;
    public void stopApplication()
    {
        try {
            MongoClient mongoClient = applicationContext.getBean(MongoClient.class);
            mongoClient.close();
            RestHighLevelClient restClient = applicationContext.getBean(RestHighLevelClient.class);
            restClient.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        SpringApplication.exit(applicationContext);
        exit(0);
    }
}
