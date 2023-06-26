package com.saptarshi.internshipprojectmdb.bufferproducer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.saptarshi.internshipprojectmdb.model.Batch;
import com.saptarshi.internshipprojectmdb.model.Payload;
import com.saptarshi.internshipprojectmdb.requestgenerator.Generator;
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
    private KafkaTemplate<String, Batch> kafkaTemplate;
    @Autowired
    private Generator generator;
    private int produced;

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaBufferProducer.class);
    public void produce() {
        List<Payload> requests=new ArrayList<Payload>();
        for(int i=0;i<batchsize;i++)
        {
            Payload payload = generator.generatePayload();
            requests.add(payload);
        }
        Batch batch=new Batch();
        batch.setBatch(requests);
        batch.setSize(requests.size());
        try {
            Message<Batch> message= MessageBuilder.withPayload(batch)
                    .setHeader(KafkaHeaders.TOPIC,"requestcollection")
                    .build();
            kafkaTemplate.send(message);

            produced+=batch.getSize();
            LOGGER.info(String.format("Documents stored in kafka=%d",produced));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
