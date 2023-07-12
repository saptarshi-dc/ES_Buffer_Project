package com.saptarshi.internshipproject.bufferconsumer;

import co.elastic.clients.elasticsearch.license.post.Acknowledgement;
import com.saptarshi.internshipproject.model.Batch;
import com.saptarshi.internshipproject.model.Payload;
import com.saptarshi.internshipproject.perfstats.ConsumerStats;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.internal.RealSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaBufferConsumerTest {
    @Mock
    private KafkaTemplate<String,Batch> kafkaTemplate;
    @Mock
    private ConsumerStats consumerStats;
    @Mock
    private RestHighLevelClient restHighLevelClient;
    @InjectMocks
    private KafkaBufferConsumer kafkaBufferConsumer;

    @Test
    void checkConsumer() throws IOException {
        Batch batch=mock(Batch.class);
        Acknowledgment acknowledgment=mock(Acknowledgment.class);

        kafkaBufferConsumer.consume(batch,acknowledgment);
    }
}