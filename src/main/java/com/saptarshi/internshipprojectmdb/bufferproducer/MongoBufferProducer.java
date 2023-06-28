package com.saptarshi.internshipprojectmdb.bufferproducer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.saptarshi.internshipprojectmdb.model.Batch;
import com.saptarshi.internshipprojectmdb.model.Payload;
import com.saptarshi.internshipprojectmdb.perfstats.ProducerStats;
import com.saptarshi.internshipprojectmdb.requestgenerator.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class MongoBufferProducer implements BufferProducer{
    @Value("${batch.size}")
    private int batchsize;
    @Autowired
    private ProducerStats producerStats;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private Generator generator;
    private static int batchnumber=0;
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoBufferProducer.class);
    public void produce() {
        List<Payload> requests=new ArrayList<Payload>();

//        long batchCreationStartTime=System.nanoTime();
        long batchCreationStartTime=System.currentTimeMillis();

        for(int i=0;i<batchsize;i++)
        {
            Payload payload = generator.generatePayload();
            requests.add(payload);
        }
        Batch batch=new Batch();
        batch.setBatch(requests);
        batch.setSize(requests.size());

//        long batchCreationEndTime=System.nanoTime();
        long batchCreationEndTime=System.currentTimeMillis();

        try {
//            long bufferInsertionStartTime=System.nanoTime();
            long bufferInsertionStartTime=System.currentTimeMillis();
//            batch.setBufferInsertionTime(bufferInsertionTime);
            mongoTemplate.save(batch);
//            long bufferInsertionEndTime=System.nanoTime();
            long bufferInsertionEndTime=System.currentTimeMillis();
            batchnumber++;

            producerStats.setBatchCreationTime(batchnumber,batchCreationEndTime-batchCreationStartTime);
            producerStats.setMongoBatchTime(batchnumber,bufferInsertionEndTime-bufferInsertionStartTime);
            LOGGER.info(String.format("Documents stored in mongodb=%d",batchnumber*batchsize));
//            LOGGER.info("Average time for documents in batch number {} to enter mongodb= {}",produced/1000,Duration.between(creationTime,Instant.now()));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static int getBatchnumber() {
        return batchnumber;
    }
}
