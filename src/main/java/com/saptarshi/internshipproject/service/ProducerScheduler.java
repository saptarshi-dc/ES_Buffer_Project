package com.saptarshi.internshipproject.service;

//import co.elastic.clients.elasticsearch.core.IndexRequest;
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch.core.IndexResponse;

import com.saptarshi.internshipproject.bufferproducer.KafkaBufferProducer;
import com.saptarshi.internshipproject.bufferproducer.MongoBufferProducer;
import com.saptarshi.internshipproject.perfstats.ProducerStats;
import com.saptarshi.internshipproject.requestgenerator.Generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ProducerScheduler {
    @Value("${producer.batch.size}")
    private int producerBatchSize;
    @Value("${buffer.type}")
    private String bufferType;
    private MongoBufferProducer mongoBufferProducer;
    private KafkaBufferProducer kafkaBufferProducer;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> producerTask;
    @Autowired
    private ProducerStats producerStats;
    @Autowired
    private Generator generator;
    private int rate;
    private boolean runningStatus;
    private Integer intervalNo;
    private Long batchCountBefore;
    private Long batchCountAfter;
    public void updateRate() {
        stopExecution();
        rate = generator.generateNewRate();
        System.out.println("New rate=" + (60000 / (10*rate))*producerBatchSize + " documents per minute");
        startExecution();
    }

    @Autowired
    public ProducerScheduler(MongoBufferProducer mongoBufferProducer,KafkaBufferProducer kafkaBufferProducer) {
        this.mongoBufferProducer=mongoBufferProducer;
        this.kafkaBufferProducer=kafkaBufferProducer;
        executorService = Executors.newSingleThreadScheduledExecutor();
        rate = 100;
        runningStatus=true;
        intervalNo=0;
    }

    public void startExecution() {
        if(isRunningStatus()==false)
            return;
        intervalNo++;
        if(bufferType.equals("mongodb"))
        {
            batchCountBefore=(long)mongoBufferProducer.getBatchnumber();
            producerTask = executorService.scheduleAtFixedRate(mongoBufferProducer::produce, 0, 10*rate, TimeUnit.MILLISECONDS);
        }
        else {
            batchCountBefore=(long)kafkaBufferProducer.getBatchnumber();
            producerTask = executorService.scheduleAtFixedRate(kafkaBufferProducer::produce, 0, 10*rate, TimeUnit.MILLISECONDS);
        }
//        batchCountBefore=(long)mongoBufferProducer.getBatchnumber();
//        batchCountBefore=(long)kafkaBufferProducer.getBatchnumber();
//        producerTask = executorService.scheduleAtFixedRate(mongoBufferProducer::produce, 0, 10*rate, TimeUnit.MILLISECONDS);
//        producerTask = executorService.scheduleAtFixedRate(kafkaBufferProducer::produce, 0, 10*rate, TimeUnit.MILLISECONDS);
    }

    public void stopExecution() {
        if (producerTask!=null) {
            System.out.println("Trying to shut down producer");
            try {
                producerTask.cancel(false);
                if(bufferType.equals("mongodb"))
                    batchCountAfter=(long)mongoBufferProducer.getBatchnumber();
                else
                    batchCountAfter=(long)kafkaBufferProducer.getBatchnumber();
                producerStats.insertIntervals(batchCountAfter);
                producerStats.setBatchesCreatedPerMinute(intervalNo,batchCountAfter-batchCountBefore);
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
