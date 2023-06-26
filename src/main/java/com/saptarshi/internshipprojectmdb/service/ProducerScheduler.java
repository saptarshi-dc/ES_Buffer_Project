package com.saptarshi.internshipprojectmdb.service;

//import co.elastic.clients.elasticsearch.core.IndexRequest;
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch.core.IndexResponse;

import com.saptarshi.internshipprojectmdb.bufferproducer.MongoBufferProducer;
import com.saptarshi.internshipprojectmdb.requestgenerator.Generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ProducerScheduler {
    private MongoBufferProducer mongoBufferInserter;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> producerTask;
    @Autowired
    private Generator generator;
    private int rate;
    private boolean runningStatus;

//    @Scheduled(fixedRate = 5000)
    public void updateRate() {
        stopExecution();
        if(isRunningStatus()==false)
            return;
        rate = generator.generateNewRate();
        System.out.println("New rate=" + (60000 / (100*rate))*1000 + " requests per minute");
        startExecution();
    }

    @Autowired
    public ProducerScheduler(MongoBufferProducer mongoBufferInserter) {
        this.mongoBufferInserter=mongoBufferInserter;
        executorService = Executors.newSingleThreadScheduledExecutor();
        rate = 20;
        runningStatus=true;
    }

    public void startExecution() {
//        producerTask = executorService.scheduleAtFixedRate(this::index, 0, rate, TimeUnit.MICROSECONDS);
        producerTask = executorService.scheduleAtFixedRate(mongoBufferInserter::produce, 0, 100*rate, TimeUnit.MILLISECONDS);
//        producerTask = executorService.scheduleAtFixedRate(this::index, 0, rate, TimeUnit.SECONDS);
    }

    public void stopExecution() {
        if (producerTask != null) {
            try {
                producerTask.cancel(false);
            } catch (Exception e) {
            }
        }
    }

    public boolean isRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(boolean runningStatus) {
        this.runningStatus = runningStatus;
    }
//    @Scheduled(fixedRate = 6)

//    public ResponseEntity<List<Payload>>getAllDocs(){
//        SearchRequest searchRequest=new SearchRequest("requests");
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        searchSourceBuilder.query(QueryBuilders.matchAllQuery()); // Match all documents
//        searchSourceBuilder.size(10000);
//        searchRequest.source(searchSourceBuilder);
//
//        try {
//            SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
//            SearchHit[] hits = searchResponse.getHits().getHits();
//            List<Payload> documents = new ArrayList<>();
//
//            for(SearchHit hit:hits){
//                String source=hit.getSourceAsString();
//                Payload document=MAPPER.readValue(source,Payload.class);
//                documents.add(document);
//            }
//            return ResponseEntity.ok(documents);
//        } catch (IOException e) {
//            LOG.error(e.getMessage(),e);
//            List<Payload> payloadList = new ArrayList<>();
//            ResponseEntity<List<Payload>> result = new ResponseEntity<>(payloadList, HttpStatus.INTERNAL_SERVER_ERROR);
//            return result;
//        }
//    }
}
