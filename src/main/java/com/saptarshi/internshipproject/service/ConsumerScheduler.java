package com.saptarshi.internshipproject.service;

import com.saptarshi.internshipproject.bufferconsumer.KafkaBufferConsumer;
import com.saptarshi.internshipproject.bufferconsumer.MongoBufferConsumer;
import com.saptarshi.internshipproject.bufferproducer.KafkaBufferProducer;
import com.saptarshi.internshipproject.bufferproducer.MongoBufferProducer;
import com.saptarshi.internshipproject.perfstats.ConsumerStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ConsumerScheduler {
    private MongoBufferConsumer mongoBufferConsumer;
    private KafkaBufferConsumer kafkaBufferConsumer;
    private ScheduledExecutorService consumerExecutorService;
    private ScheduledExecutorService consumerStopExecutorService;
    private ScheduledFuture<?>[] consumerTask;
    @Autowired
    private ConsumerStats consumerStats;
    @Autowired
    private KafkaProperties kafkaProperties;
    private Integer iterationNo;
    private Long batchesConsumedBefore;
    private Long batchesConsumedAfter;
    @Autowired
    private ProducerScheduler producerScheduler;
    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;
    @Value("${listener.id}")
    private String listenerId;
    private boolean runningStatus;
    @Value("${batch.size}")
    private int batchSize;
    private int consumers;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerScheduler.class);

    @Autowired
    public ConsumerScheduler(@Value("${consumers}") int consumers, MongoBufferConsumer mongoBufferConsumer, KafkaBufferConsumer kafkaBufferConsumer, Shutdown shutdown) {
        this.mongoBufferConsumer = mongoBufferConsumer;
        this.kafkaBufferConsumer = kafkaBufferConsumer;
        this.consumers = consumers;
        consumerExecutorService = Executors.newScheduledThreadPool(consumers);
        consumerStopExecutorService = Executors.newScheduledThreadPool(consumers);
        runningStatus = true;
        consumerTask = new ScheduledFuture<?>[consumers];
        iterationNo = 0;
    }

    public void mongoConsumerScheduler() {
        if (isRunningStatus() == false)
            return;
        iterationNo++;
        batchesConsumedBefore = (long) mongoBufferConsumer.getBatchnumber();
        for (int i = 0; i < consumers; i++)
            consumerTask[i] = consumerExecutorService.schedule(mongoBufferConsumer::consume, 0, TimeUnit.SECONDS);

        for (int i = 0; i < consumers; i++) {
            int finalI = i;
            consumerStopExecutorService.schedule(() -> {
                System.out.println("Entered consumer shutdown");
                if (!consumerTask[finalI].isDone()) {
                    consumerTask[finalI].cancel(false);
                    mongoBufferConsumer.setCancelled(true);
                }
                if(finalI==consumers-1) {
                    batchesConsumedAfter = (long) mongoBufferConsumer.getBatchnumber();
                    consumerStats.setBatchesConsumedPerIteration(iterationNo, batchesConsumedAfter - batchesConsumedBefore);
                    LOGGER.info("Number of documents indexed into Elasticsearch = {}", batchesConsumedAfter * batchSize);
                }
                if (producerScheduler.isRunningStatus() == false && MongoBufferProducer.getBatchnumber() <= MongoBufferConsumer.getBatchnumber()) {
                    setRunningStatus(false);
                }
            }, 20, TimeUnit.SECONDS);
        }
    }

    public void kafkaConsumerScheduler() {
        if (isRunningStatus() == false)
            return;
        iterationNo++;

        String partitionerClass = kafkaProperties.getProducer().getProperties().get("partitioner.class");
        System.out.println("Partitioning Strategy: " + partitionerClass);

        batchesConsumedBefore = (long) kafkaBufferConsumer.getBatchnumber();

        endpointRegistry.getListenerContainer(listenerId).resume();

        consumerStopExecutorService.schedule(() -> {
            System.out.println("Entered consumer shutdown");
            endpointRegistry.getListenerContainer(listenerId).pause();
            batchesConsumedAfter = (long) kafkaBufferConsumer.getBatchnumber();
            consumerStats.setBatchesConsumedPerIteration(iterationNo, batchesConsumedAfter - batchesConsumedBefore);
            LOGGER.info("Number of documents indexed into Elasticsearch = {}", batchesConsumedAfter * batchSize);
            if (producerScheduler.isRunningStatus() == false && KafkaBufferProducer.getBatchnumber() <= KafkaBufferConsumer.getBatchnumber()) {
                setRunningStatus(false);
            }
        }, 20, TimeUnit.SECONDS);
    }

    public boolean isRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(boolean runningStatus) {
        this.runningStatus = runningStatus;
    }
}
