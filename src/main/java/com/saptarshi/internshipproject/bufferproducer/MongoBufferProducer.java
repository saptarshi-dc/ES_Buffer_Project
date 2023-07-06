package com.saptarshi.internshipproject.bufferproducer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.saptarshi.internshipproject.model.Batch;
import com.saptarshi.internshipproject.model.Payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.saptarshi.internshipproject.model.Batch;
import com.saptarshi.internshipproject.model.Payload;
import com.saptarshi.internshipproject.perfstats.ProducerStats;
import com.saptarshi.internshipproject.requestgenerator.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MongoBufferProducer implements BufferProducer {
    @Value("${batch.size}")
    private int batchsize;
    @Value("${buffer.collectionname}")
    private String collectionName;
    @Autowired
    private ProducerStats producerStats;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private Generator generator;
    private static int batchnumber = 0;
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoBufferProducer.class);

    public void produce() {
        List<Payload> requests = new ArrayList<Payload>();

//        long batchCreationStartTime=System.nanoTime();
        long batchCreationStartTime = System.currentTimeMillis();

        for (int i = 0; i < batchsize; i++) {
            Payload payload = generator.generatePayload();
            requests.add(payload);
        }
        Batch batch = new Batch();
        batch.setRequests(requests);
        batch.setSize(requests.size());
        batch.setBatchnumber(batchnumber+1);
//        batch.setConsumedBy(0);

//        long batchCreationEndTime=System.nanoTime();
        long batchCreationEndTime = System.currentTimeMillis();
        batch.setCreationTime(batchCreationEndTime);

        try {
//            long bufferInsertionStartTime=System.nanoTime();
            long bufferInsertionStartTime = System.currentTimeMillis();
//            batch.setBufferInsertionTime(bufferInsertionTime);
            mongoTemplate.save(batch, collectionName);
//            long bufferInsertionEndTime=System.nanoTime();
            long bufferInsertionEndTime = System.currentTimeMillis();
            batchnumber++;

            producerStats.setBatchCreationTime(batchnumber, batchCreationEndTime - batchCreationStartTime);
            producerStats.setBufferBatchTime(batchnumber, bufferInsertionEndTime - bufferInsertionStartTime);
            LOGGER.info(String.format("Documents stored in mongodb=%d", batchnumber * batchsize));
//            LOGGER.info("Average time for documents in batch number {} to enter mongodb= {}",produced/1000,Duration.between(creationTime,Instant.now()));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static int getBatchnumber(){
        return batchnumber;
    }
}


//        batchPartitionSave(batch,batchCreationStartTime);
//    }
//    public void batchPartitionSave(Batch batch,long batchCreationStartTime){
//        List<Payload>requests=batch.getRequests();
//        List<Payload>partitionedRequests=new ArrayList<>();
//        try {
//            long bufferInsertionStartTime=System.currentTimeMillis();
//            for (int i = 0; i < requests.size(); i++) {
//                partitionedRequests.add(requests.get(i));
//                if (i == requests.size() - 1 || partitionedRequests.size() == consumerBatchSize) {
//                    Batch partitionedBatch = new Batch();
//                    partitionedBatch.setRequests(partitionedRequests);
//                    partitionedBatch.setSize(partitionedRequests.size());
//                    partitionedBatch.setCreationTime(batch.getCreationTime());
//                    partitionedBatch.setBatchnumber(batch.getBatchnumber());
//                    mongoTemplate.save(partitionedBatch, collectionName);
//                    partitionedRequests=new ArrayList<>();
//                }
//            }
//            long bufferInsertionEndTime = System.currentTimeMillis();
//
//            docsProduced+=requests.size();
//            producerStats.setBatchCreationTime(batchnumber, batch.getCreationTime() - batchCreationStartTime);
//            producerStats.setBufferBatchTime(batchnumber, bufferInsertionEndTime - bufferInsertionStartTime);
//            LOGGER.info(String.format("Documents stored in mongodb=%d",getDocsProduced()));
//        }catch (final Exception e) {
//            LOGGER.error(e.getMessage(), e);
//        }
//    }
//    public static long getDocsProduced() {
//        return docsProduced;
//    }
