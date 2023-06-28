package com.saptarshi.internshipprojectmdb.bufferconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.saptarshi.internshipprojectmdb.model.Batch;
import com.saptarshi.internshipprojectmdb.model.Payload;
import com.saptarshi.internshipprojectmdb.perfstats.ConsumerStats;
import com.saptarshi.internshipprojectmdb.perfstats.ProducerStats;
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
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${batch.size}")
    private int batchsize;
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
//    private static Duration totalBatchIndexingTime=Duration.ZERO;
//    private static Duration totalBatchBufferTime=Duration.ZERO;

    public void consume() {
        LOGGER.info("Starting new consumer:");
        isCancelled=false;
        while (!isCancelled) {
//            long batchProcessingStartTime=System.nanoTime();
            long batchProcessingStartTime=System.currentTimeMillis();

            Query query = new Query();
            query.with(Sort.by("$natural").ascending());
            Batch batch = mongoTemplate.findOne(query, Batch.class);
            if (batch == null)
                break;

            BulkRequest br = new BulkRequest();

//            ObjectId id=new ObjectId(batch.getId());
//            int timestamp=id.getTimestamp();
//            Date date=new Date(timestamp*1000L);
//            Instant bufferInsertionTime=date.toInstant();
//            Instant bufferInsertionTime=batch.getBufferInsertionTime();

            Instant indexingTime=Instant.now();

            Duration batchIndexingTime;
            Duration mongoIndexingTime=Duration.ZERO;
            Duration totalInsertionTime=Duration.ZERO;

            for (Payload request : batch.getRequests()) {
                try {
//                    request.setBufferInsertionTime(bufferInsertionTime);
                    Instant esInsertionTime=Instant.now();
                    request.setEsInsertionTime(esInsertionTime);

//                    mongoIndexingTime=mongoIndexingTime.plus(Duration.between(request.getCreationTime(),request.getBufferInsertionTime()));
                    totalInsertionTime=totalInsertionTime.plus(Duration.between(request.getCreationTime(),request.getEsInsertionTime()));

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
                mongoTemplate.remove(batch);

                consumerStats.setBatchProcessingTime(batchnumber,batchProcessingEndTime-batchProcessingStartTime);
                consumerStats.setEsBatchTime(batchnumber,esInsertionEndTime-esInsertionStartTime);
//                batchIndexingTime=Duration.between(indexingTime,Instant.now());
//                LOGGER.info("Average time for documents in batch number {} to enter mongodb= {}",batchnumber,mongoIndexingTime.dividedBy(1000));
//                LOGGER.info("Average indexing time for documents in this batch= {}",batchIndexingTime);
                LOGGER.info("Average time taken for documents in batch number {} from start to finish= {}",batchnumber,totalInsertionTime.dividedBy(1000));
//                totalBatchBufferTime=totalBatchBufferTime.plus(mongoIndexingTime.dividedBy(1000));
//                totalBatchIndexingTime=totalBatchIndexingTime.plus(totalInsertionTime.dividedBy(1000));

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.info("Number of documents indexed into Elasticsearch = {}",getBatchnumber()*batchsize);
    }

//    public static Duration getTotalBatchIndexingTime() {
//        return totalBatchIndexingTime;
//    }
//    public static Duration getTotalBatchBufferTime() {
//        return totalBatchBufferTime;
//    }
    public static int getBatchnumber() {
        return batchnumber;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}