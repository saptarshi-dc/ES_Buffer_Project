package com.saptarshi.internshipprojectmdb.service;

import com.saptarshi.internshipprojectmdb.bufferconsumer.MongoBufferConsumer;
import com.saptarshi.internshipprojectmdb.bufferproducer.MongoBufferProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class MasterScheduler implements CommandLineRunner {
    @Autowired
    private ProducerScheduler producerScheduler;
    @Autowired
    private Shutdown shutdown;
    @Autowired
    private ConsumerScheduler consumerScheduler;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> producerTask;
    private ScheduledFuture<?> consumerTask;
    private static final Logger LOGGER= LoggerFactory.getLogger(MasterScheduler.class);

    @Override
    public void run(String... args) {
        executorService = Executors.newSingleThreadScheduledExecutor();
        producerTask = executorService.scheduleAtFixedRate(producerScheduler::updateRate, 0, 5000, TimeUnit.MILLISECONDS);
        consumerTask = executorService.scheduleAtFixedRate(consumerScheduler::scheduler, 1000, 4000, TimeUnit.MILLISECONDS);

        executorService.schedule(() -> {
            producerTask.cancel(false);
            producerScheduler.stopExecution();
            producerScheduler.setRunningStatus(false);
        }, 60000, TimeUnit.MILLISECONDS);
    }

    @Scheduled(initialDelay=60000,fixedRate = 1000)
    public void stopExecution() {
        if (consumerScheduler.isRunningStatus()==false) {
            consumerTask.cancel(false);
            LOGGER.info("Average time for 1 batch to enter mongodb={}",MongoBufferConsumer.getTotalBatchBufferTime().dividedBy(MongoBufferConsumer.getBatchnumber()));
            LOGGER.info("Average time for 1 batch from start to finish={}",MongoBufferConsumer.getTotalBatchIndexingTime().dividedBy(MongoBufferConsumer.getBatchnumber()));
            shutdown.stopApplication();
        }
    }
}
