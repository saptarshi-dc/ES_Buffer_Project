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
    @Value("${kafka.index.name}")
    private String indexName;
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
        long batchProcessingStartTime=System.currentTimeMillis();
        BulkRequest br = new BulkRequest();
        for (Payload request : batch.getRequests()) {
            try {
                br.add(new IndexRequest(indexName)
                        .id(String.valueOf(request.getId()))
                        .source(objectMapper.writeValueAsString(request), XContentType.JSON));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
            }
        }
        long batchProcessingEndTime=System.currentTimeMillis();

        try {
            long esInsertionStartTime=System.currentTimeMillis();

            BulkResponse bulkResponse = client.bulk(br, RequestOptions.DEFAULT);

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

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}