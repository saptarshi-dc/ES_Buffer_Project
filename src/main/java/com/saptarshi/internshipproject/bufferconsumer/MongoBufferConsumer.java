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
import org.springframework.data.domain.Sort;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class MongoBufferConsumer{
    @Value("${batch.size}")
    private int batchsize;
    @Value("${buffer.collectionname}")
    private String collectionName;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private ConsumerStats consumerStats;
    private boolean isCancelled;
    private static int batchnumber=0;
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Logger LOGGER= LoggerFactory.getLogger(MongoBufferConsumer.class);

    public void consume() {
        LOGGER.info("Starting new consumer:");
        isCancelled=false;
        while (!isCancelled) {
//            long batchProcessingStartTime=System.nanoTime();
            long batchProcessingStartTime=System.currentTimeMillis();

            Query query = new Query();
            query.with(Sort.by("$natural").ascending());
            Batch batch = mongoTemplate.findOne(query, Batch.class,collectionName);
            if (batch == null)
                continue;

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

//            long batchProcessingEndTime=System.nanoTime();
            long batchProcessingEndTime=System.currentTimeMillis();

            try {
//                long esInsertionStartTime=System.nanoTime();
                long esInsertionStartTime=System.currentTimeMillis();

                BulkResponse bulkResponse = client.bulk(br, RequestOptions.DEFAULT);

//                long esInsertionEndTime=System.nanoTime();
                long esInsertionEndTime=System.currentTimeMillis();
                batchnumber++;
                mongoTemplate.remove(batch,collectionName);

                consumerStats.setBatchProcessingTime(batchnumber,batchProcessingEndTime-batchProcessingStartTime);
                consumerStats.setEsBatchTime(batchnumber,esInsertionEndTime-esInsertionStartTime);
                consumerStats.setBatchTotalTime(batchnumber,esInsertionEndTime-batch.getCreationTime());

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.info("Number of documents indexed into Elasticsearch = {}",getBatchnumber()*batchsize);
    }
    public static int getBatchnumber() {
        return batchnumber;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}