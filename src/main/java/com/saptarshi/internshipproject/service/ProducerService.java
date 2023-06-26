package com.saptarshi.internshipproject.service;

//import co.elastic.clients.elasticsearch.core.IndexRequest;
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch.core.IndexResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saptarshi.internshipproject.model.Payload;
import com.saptarshi.internshipproject.requestgenerator.Generator;
import org.elasticsearch.action.DocWriteRequest;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ProducerService{
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private Generator generator;
//    private ScheduledTaskRegistrar taskRegistrar;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> producerTask;

    private int total=0;
    private int rate;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER= LoggerFactory.getLogger(ProducerService.class);
//    @Override
//    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//        this.taskRegistrar = taskRegistrar; // Assign taskRegistrar to the instance variable
//        scheduleTask(taskRegistrar);
//        producerTask=taskRegistrar.getScheduler().scheduleAtFixedRate(()->index(), rate);
//        taskScheduler=new ThreadPoolTaskScheduler();
//        taskScheduler.initialize();
//        taskRegistrar.setTaskScheduler(taskScheduler);
//        taskRegistrar.addFixedRateTask(() -> index(), rate);
//        producerTask=taskRegistrar.getScheduler().schedule();
//    }
//
    @Scheduled(fixedRate=5000)
    public void updateRate(){
        rate=generator.generateNewRate();
        System.out.println("New rate="+60000/rate+" requests per minute");
        stopExecution();
        startExecution();
    }
    public ProducerService(){
        executorService= Executors.newSingleThreadScheduledExecutor();
        rate=6;
    }

    public void startExecution() {
        producerTask = executorService.scheduleAtFixedRate(this::index, 0, rate, TimeUnit.MILLISECONDS);
    }

    public void stopExecution() {
        if (producerTask!= null) {
            try {
                producerTask.cancel(false);
            } catch (Exception e) {
            }
        }
    }


//    @Scheduled(fixedRate = 6)
    public Boolean index(){
        Payload payload=generator.generatePutRequest();
        try{
            final String vehicleAsString=MAPPER.writeValueAsString(payload);

//            IndexRequest<Payload>request=new IndexRequest.Builder<>()
//                    .index("requests")
//                    .id(payload.getId())
//                    .document(payload)
//                    .build();

            final IndexRequest request=new IndexRequest("requests");
            request.id(String.valueOf(payload.getId()));
            request.source(vehicleAsString, XContentType.JSON);

            final IndexResponse response=client.index(request, RequestOptions.DEFAULT);
            total++;
            LOGGER.info(String.format("Documents indexed in elasticsearch=%d",total));
            return response!=null&&response.status().equals(RestStatus.OK);

        }catch (final Exception e){
            LOGGER.error(e.getMessage(),e);
            return false;
        }
    }

    public ResponseEntity<List<Payload>>getAllDocs(){
        SearchRequest searchRequest=new SearchRequest("requests");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.matchAllQuery()); // Match all documents
        searchSourceBuilder.size(10000);
        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
            SearchHit[] hits = searchResponse.getHits().getHits();
            List<Payload> documents = new ArrayList<>();

            for(SearchHit hit:hits){
                String source=hit.getSourceAsString();
                Payload document=MAPPER.readValue(source,Payload.class);
                documents.add(document);
            }
            return ResponseEntity.ok(documents);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(),e);
            List<Payload> payloadList = new ArrayList<>();
            ResponseEntity<List<Payload>> result = new ResponseEntity<>(payloadList, HttpStatus.INTERNAL_SERVER_ERROR);
            return result;
        }
    }
}
