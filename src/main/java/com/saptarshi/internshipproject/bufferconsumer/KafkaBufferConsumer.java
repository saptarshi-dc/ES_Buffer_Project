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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

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

    @KafkaListener(id="kafkaBuffer",topics = "requestcollection", groupId = "consumerGroup",autoStartup = "false")
    public void consume(Batch batch, Acknowledgment acknowledgment) {
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

            consumerStats.setBatchProcessingTime(batchnumber,batchProcessingEndTime-batchProcessingStartTime);
            consumerStats.setEsBatchTime(batchnumber,esInsertionEndTime-esInsertionStartTime);
            consumerStats.setBatchTotalTime(batchnumber,esInsertionEndTime-batch.getCreationTime());

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