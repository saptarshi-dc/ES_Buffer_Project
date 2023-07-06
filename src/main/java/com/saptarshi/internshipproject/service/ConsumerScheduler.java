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
    private Integer iterationNo;
    private Long batchesConsumedBefore;
    private Long batchesConsumedAfter;
    @Autowired
    private ProducerScheduler producerScheduler;
    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;
    //    @Value("${listener.id}")
//    private String listenerId;
    private boolean runningStatus;
    @Value("${producer.batch.size}")
    private int producerBatchSize;
    @Value("${consumer.batch.size}")
    private int consumerBatchSize;
    private int consumers;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerScheduler.class);

    @Autowired
    public ConsumerScheduler(@Value("${consumers}") int consumers, MongoBufferConsumer mongoBufferConsumer, KafkaBufferConsumer kafkaBufferConsumer, Shutdown shutdown) {
        this.mongoBufferConsumer = mongoBufferConsumer;
        this.kafkaBufferConsumer = kafkaBufferConsumer;
        this.consumers = consumers;
//        consumers=10;
//        consumerExecutorService = Executors.newSingleThreadScheduledExecutor();
        consumerExecutorService = Executors.newScheduledThreadPool(consumers);
        consumerStopExecutorService = Executors.newScheduledThreadPool(consumers);
        runningStatus = true;
        consumerTask = new ScheduledFuture<?>[consumers];
        iterationNo = 0;
    }

    //    @Scheduled(fixedRate = 4000)
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
                batchesConsumedAfter = (long) mongoBufferConsumer.getBatchnumber();
                consumerStats.setBatchesConsumedPerIteration(iterationNo, batchesConsumedAfter - batchesConsumedBefore);
                LOGGER.info("Number of documents indexed into Elasticsearch = {}", batchesConsumedAfter * consumerBatchSize);
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
        batchesConsumedBefore = (long) kafkaBufferConsumer.getBatchnumber();
//        MessageListenerContainer[] listenerContainer = new MessageListenerContainer[consumers];
//        for(int i=0;i<consumers;i++) {
//            String listenerId=""+i;
//        System.out.println("hello");
//            listenerContainer[i] = endpointRegistry.getListenerContainer(listenerId);
//            System.out.println("Listener container="+listenerContainer[i].toString());
//            listenerContainer[i].start();
        System.out.println("number of consumers="+endpointRegistry.getListenerContainers().size());
        endpointRegistry.start();
//        System.out.println("started");
//            int finalI = i;
        consumerStopExecutorService.schedule(() -> {
            System.out.println("Entered consumer shutdown");
//                listenerContainer[finalI].stop();
            endpointRegistry.stop();
            batchesConsumedAfter = (long) kafkaBufferConsumer.getBatchnumber();
            consumerStats.setBatchesConsumedPerIteration(iterationNo, batchesConsumedAfter - batchesConsumedBefore);
            LOGGER.info("Number of documents indexed into Elasticsearch = {}", batchesConsumedAfter * consumerBatchSize);
            if (producerScheduler.isRunningStatus() == false && KafkaBufferProducer.getBatchnumber() <= KafkaBufferConsumer.getBatchnumber()) {
                setRunningStatus(false);
            }
        }, 20, TimeUnit.SECONDS);
    }

//}

    public boolean isRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(boolean runningStatus) {
        this.runningStatus = runningStatus;
    }
}
