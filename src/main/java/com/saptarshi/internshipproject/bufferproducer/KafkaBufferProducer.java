package com.saptarshi.internshipproject.bufferproducer;

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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KafkaBufferProducer implements BufferProducer{
    @Value("${producer.batch.size}")
    private int producerBatchSize;
    @Autowired
    private ProducerStats producerStats;
    @Autowired
    private KafkaTemplate<String, Batch> kafkaTemplate;
    @Autowired
    private Generator generator;
    private static int batchnumber=0;
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaBufferProducer.class);
    public void produce() {
        List<Payload> requests=new ArrayList<Payload>();
        //        long batchCreationStartTime=System.nanoTime();
        long batchCreationStartTime=System.currentTimeMillis();
        for(int i=0;i<producerBatchSize;i++)
        {
            Payload payload = generator.generatePayload();
            requests.add(payload);
        }
        Batch batch=new Batch();
        batch.setRequests(requests);
        batch.setSize(requests.size());
        batch.setBatchnumber(batchnumber+1);
//        long batchCreationEndTime=System.nanoTime();
        long batchCreationEndTime=System.currentTimeMillis();
        batch.setCreationTime(batchCreationEndTime);
        try {
//            long bufferInsertionStartTime=System.nanoTime();
            long bufferInsertionStartTime=System.currentTimeMillis();
            Message<Batch> message= MessageBuilder.withPayload(batch)
                    .setHeader(KafkaHeaders.TOPIC,"requestcollection")
                    .build();
            kafkaTemplate.send(message);
//            long bufferInsertionEndTime=System.nanoTime();
            long bufferInsertionEndTime=System.currentTimeMillis();
            batchnumber++;

            producerStats.setBatchCreationTime(batchnumber,batchCreationEndTime-batchCreationStartTime);
            producerStats.setBufferBatchTime(batchnumber,bufferInsertionEndTime-bufferInsertionStartTime);
            LOGGER.info(String.format("Documents stored in kafka=%d",batchnumber*producerBatchSize));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    public void batchPartitionSave(Batch batch,long batchCreationStartTime){
//        List<Payload>requests=batch.getRequests();
//        List<Payload>partitionedRequests=new ArrayList<>();
//        long bufferInsertionStartTime=System.currentTimeMillis();
//        try {
//            for (int i = 0; i < requests.size(); i++) {
//                partitionedRequests.add(requests.get(i));
//                if (i == requests.size() - 1 || partitionedRequests.size() == 1000) {
//                    Batch partitionedBatch = new Batch();
//                    partitionedBatch.setRequests(partitionedRequests);
//                    partitionedBatch.setSize(partitionedRequests.size());
//                    partitionedBatch.setCreationTime(batch.getCreationTime());
//                    mongoTemplate.save(partitionedBatch, collectionName);
//                    partitionedRequests=new ArrayList<>();
//                }
//            }
//            batchnumber++;
//            long bufferInsertionEndTime = System.currentTimeMillis();
//            producerStats.setBatchCreationTime(batchnumber, batch.getCreationTime() - batchCreationStartTime);
//            producerStats.setBufferBatchTime(batchnumber, bufferInsertionEndTime - bufferInsertionStartTime);
//            LOGGER.info(String.format("Documents stored in mongodb=%d",batchnumber*producerBatchSize));
//        }catch (final Exception e) {
//            LOGGER.error(e.getMessage(), e);
//        }
    }
    public static int getBatchnumber() {
        return batchnumber;
    }
}

