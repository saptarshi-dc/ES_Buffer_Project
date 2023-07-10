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
    @Value("${batch.size}")
    private int batchsize;
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
        long batchCreationStartTime=System.currentTimeMillis();
        for(int i=0;i<batchsize;i++)
        {
            Payload payload = generator.generatePayload();
            requests.add(payload);
        }
        Batch batch=new Batch();
        batch.setRequests(requests);
        batch.setSize(requests.size());
        batch.setBatchnumber(batchnumber+1);
        long batchCreationEndTime=System.currentTimeMillis();
        batch.setCreationTime(batchCreationEndTime);
        try {
            long bufferInsertionStartTime=System.currentTimeMillis();
            Message<Batch> message= MessageBuilder.withPayload(batch)
                    .setHeader(KafkaHeaders.TOPIC,"requestcollection")
                    .setHeader(KafkaHeaders.PARTITION_ID,generator.generatePartitionNumber(batch.getBatchnumber()))
                    .build();

            kafkaTemplate.send(message);
            long bufferInsertionEndTime=System.currentTimeMillis();
            batchnumber++;

            producerStats.setBatchCreationTime(batch.getBatchnumber(),batchCreationEndTime-batchCreationStartTime);
            producerStats.setBufferBatchTime(batch.getBatchnumber(),bufferInsertionEndTime-bufferInsertionStartTime);
            LOGGER.info(String.format("Documents stored in kafka=%d",batchnumber*batchsize));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    public static int getBatchnumber() {
        return batchnumber;
    }
}

