package com.saptarshi.internshipprojectmdb.bufferconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.saptarshi.internshipprojectmdb.model.Batch;
import com.saptarshi.internshipprojectmdb.model.Payload;
import org.bson.types.ObjectId;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class MongoBufferConsumer implements BufferConsumer {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RestHighLevelClient client;
    private static int batchnumber=0;
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Logger LOGGER= LoggerFactory.getLogger(MongoBufferConsumer.class);
    private static Duration totalBatchIndexingTime=Duration.ZERO;
    private static Duration totalBatchBufferTime=Duration.ZERO;

    public void consume() {
        while (true) {
            Query query = new Query();
            query.with(Sort.by("$natural").ascending());
            Batch batch = mongoTemplate.findOne(query, Batch.class);
            if (batch == null)
                return;

            BulkRequest br = new BulkRequest();

//            ObjectId id=new ObjectId(batch.getId());
//            int timestamp=id.getTimestamp();
//            Date date=new Date(timestamp*1000L);
//            Instant bufferInsertionTime=date.toInstant();
            Instant bufferInsertionTime=batch.getBufferInsertionTime();

            Instant indexingTime=Instant.now();

            Duration batchIndexingTime;
            Duration mongoIndexingTime=Duration.ZERO;
            Duration totalInsertionTime=Duration.ZERO;

            for (Payload request : batch.getRequests()) {
                try {
                    request.setBufferInsertionTime(bufferInsertionTime);
                    Instant esInsertionTime=Instant.now();
                    request.setEsInsertionTime(esInsertionTime);

                    mongoIndexingTime=mongoIndexingTime.plus(Duration.between(request.getCreationTime(),request.getBufferInsertionTime()));
                    totalInsertionTime=totalInsertionTime.plus(Duration.between(request.getCreationTime(),request.getEsInsertionTime()));

                    br.add(new IndexRequest("requests_index")
                            .id(String.valueOf(request.getId()))
                            .source(objectMapper.writeValueAsString(request), XContentType.JSON));

                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            try {
                BulkResponse bulkResponse = client.bulk(br, RequestOptions.DEFAULT);
                batchnumber++;
                mongoTemplate.remove(batch);
//                batchIndexingTime=Duration.between(indexingTime,Instant.now());
                LOGGER.info("Average time for documents in batch number {} to enter mongodb= {}",batchnumber,mongoIndexingTime.dividedBy(1000));
//                LOGGER.info("Average indexing time for documents in this batch= {}",batchIndexingTime);
                LOGGER.info("Average time taken for documents in batch number {} from start to finish= {}",batchnumber,totalInsertionTime.dividedBy(1000));
                totalBatchBufferTime=totalBatchBufferTime.plus(mongoIndexingTime.dividedBy(1000));
                totalBatchIndexingTime=totalBatchIndexingTime.plus(totalInsertionTime.dividedBy(1000));

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static Duration getTotalBatchIndexingTime() {
        return totalBatchIndexingTime;
    }
    public static Duration getTotalBatchBufferTime() {
        return totalBatchBufferTime;
    }
    public static int getBatchnumber() {
        return batchnumber;
    }
}