package com.saptarshi.internshipproject.bufferconsumer;

import com.saptarshi.internshipproject.model.Batch;
import com.saptarshi.internshipproject.model.Payload;
import com.saptarshi.internshipproject.perfstats.ConsumerStats;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MongoBufferConsumerTest {
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private ConsumerStats consumerStats;
    @Mock
    private RestHighLevelClient restHighLevelClient;
    @Mock
    private ReentrantLock bufferLock;
    @InjectMocks
    private MongoBufferConsumer mongoBufferConsumer;

    @Test
    void checkConsumer() throws IOException {
        String collectionName="requestcollection";
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        mongoBufferConsumer.setCollectionName(collectionName);
        mongoBufferConsumer.setIndexName("mongodb_index");

        executorService.schedule(() -> {
            mongoBufferConsumer.setCancelled(true);
        }, 500, TimeUnit.MILLISECONDS);
        mongoBufferConsumer.consume();

        verify(mongoTemplate,atLeastOnce()).findOne(any(Query.class),eq(Batch.class),anyString());
    }
}