package com.saptarshi.internshipproject.bufferproducer;

import com.saptarshi.internshipproject.model.Batch;
import com.saptarshi.internshipproject.perfstats.ProducerStats;
import com.saptarshi.internshipproject.requestgenerator.Generator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MongoBufferProducerTest {
    @Mock
    private Generator generator;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private ProducerStats producerStats;
    @InjectMocks
    private MongoBufferProducer mongoBufferProducer;

    @Test
    void checkProducer() {
        int batchsize=1000;
        String collectionName="requestcollection";
        mongoBufferProducer.setBatchSize(batchsize);
        mongoBufferProducer.setCollectionName(collectionName);
        mongoBufferProducer.produce();

        verify(generator,times(batchsize)).generatePayload(); //Check whether the required number of batches is created
        verify(mongoTemplate).save(any(Batch.class),eq(collectionName)); //Check whether write request was sent to mongodb

        verify(producerStats).setBatchCreationTime(anyInt(),anyLong()); //Check whether producer stats gets updated
        verify(producerStats).setBufferBatchTime(anyInt(),anyLong());
    }
}
