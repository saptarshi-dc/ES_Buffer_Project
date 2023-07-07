package com.saptarshi.internshipproject.bufferconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.saptarshi.internshipproject.model.Batch;
import com.saptarshi.internshipproject.model.Payload;
import com.saptarshi.internshipproject.perfstats.ConsumerStats;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.kafka.support.KafkaHeaders;


@Component
public class KafkaBufferConsumer{
    @Value("${consumer.batch.size}")
    private int consumerBatchSize;
    @Autowired
    private KafkaTemplate<String, Batch> kafkaTemplate;
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private ConsumerStats consumerStats;
    private boolean isCancelled;
    private static int batchnumber=0;
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Logger LOGGER= LoggerFactory.getLogger(KafkaBufferConsumer.class);

    @KafkaListener(id="kafkaBuffer",topics = "requestcollection", groupId = "consumerGroup",autoStartup = "true")
    public void consume(Batch batch, Acknowledgment acknowledgment) {
//        System.out.println("Starting new consumer");
//        System.out.println("consumer id="+consumerId);
        long batchProcessingStartTime=System.currentTimeMillis();
        BulkRequest br = new BulkRequest();
        for (Payload request : batch.getRequests()) {
            try {
                br.add(new IndexRequest("requests_index")
                        .id(String.valueOf(request.getId()))
                        .source(objectMapper.writeValueAsString(request), XContentType.JSON));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
            }
        }
//        long batchProcessingEndTime=System.nanoTime();
        long batchProcessingEndTime=System.currentTimeMillis();

        try {
//                long esInsertionStartTime=System.nanoTime();
            long esInsertionStartTime=System.currentTimeMillis();

            BulkResponse bulkResponse = client.bulk(br, RequestOptions.DEFAULT);

//                long esInsertionEndTime=System.nanoTime();
            long esInsertionEndTime=System.currentTimeMillis();
            batchnumber++;
            acknowledgment.acknowledge();

            consumerStats.setBatchProcessingTime(batch.getBatchnumber(),batchProcessingEndTime-batchProcessingStartTime);
            consumerStats.setEsBatchTime(batch.getBatchnumber(),esInsertionEndTime-esInsertionStartTime);
            consumerStats.setBatchTotalTime(batch.getBatchnumber(),esInsertionEndTime-batch.getCreationTime());

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    public static int getBatchnumber() {
        return batchnumber;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}