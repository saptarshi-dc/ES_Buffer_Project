package com.saptarshi.internshipproject.bufferproducer;

import com.saptarshi.internshipproject.perfstats.ProducerStats;
import com.saptarshi.internshipproject.requestgenerator.Generator;
import org.springframework.messaging.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaBufferProducerTest {
    @Mock
    private Generator generator;
    @Mock
    private KafkaTemplate kafkaTemplate;
    @Mock
    private ProducerStats producerStats;
    @InjectMocks
    private KafkaBufferProducer kafkaBufferProducer;

    @Test
    void checkProducer() {
        int batchsize=1000;
        kafkaBufferProducer.setBatchSize(batchsize);
        kafkaBufferProducer.produce();

        verify(generator,times(batchsize)).generatePayload(); //Check whether the required number of batches is created
        verify(kafkaTemplate).send(any(Message.class)); //Check whether write request was sent to kafka

        verify(producerStats).setBatchCreationTime(anyInt(),anyLong()); //Check whether producer stats gets updated
        verify(producerStats).setBufferBatchTime(anyInt(),anyLong());
    }
}
