package com.saptarshi.internshipprojectmdb.service;

import com.saptarshi.internshipprojectmdb.bufferconsumer.MongoBufferConsumer;
import com.saptarshi.internshipprojectmdb.bufferproducer.MongoBufferProducer;
import com.saptarshi.internshipprojectmdb.perfstats.ConsumerStats;
import com.saptarshi.internshipprojectmdb.perfstats.ProducerStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ConsumerScheduler {
    private MongoBufferConsumer mongoBufferConsumer;
    private ScheduledExecutorService consumerExecutorService;
    private ScheduledExecutorService consumerStopExecutorService;
    private ScheduledFuture<?> consumerTask;
    @Autowired
    private ConsumerStats consumerStats;
    private Integer iterationNo;
    private Long batchesConsumedBefore;
    private Long batchesConsumedAfter;
    @Autowired
    private ProducerScheduler producerScheduler;
    private boolean runningStatus;
    @Value("${batch.size}")
    private int batchsize;
    private static final Logger LOGGER=LoggerFactory.getLogger(ConsumerScheduler.class);

    @Autowired
    public ConsumerScheduler(MongoBufferConsumer mongoBufferConsumer,Shutdown shutdown) {
        this.mongoBufferConsumer=mongoBufferConsumer;
        consumerExecutorService = Executors.newSingleThreadScheduledExecutor();
        consumerStopExecutorService = Executors.newSingleThreadScheduledExecutor();
        runningStatus=true;
        iterationNo=0;
    }
//    @Scheduled(fixedRate = 4000)
    public void scheduler(){
        if(isRunningStatus()==false)
            return;
        iterationNo++;
        batchesConsumedBefore=(long)mongoBufferConsumer.getBatchnumber();
        consumerTask=consumerExecutorService.schedule(mongoBufferConsumer::consume,0,TimeUnit.SECONDS);
        consumerStopExecutorService.schedule(() -> {
            System.out.println("Entered consumer shutdown");
            if (!consumerTask.isDone()) {
                consumerTask.cancel(false);
                mongoBufferConsumer.setCancelled(true);
            }
            batchesConsumedAfter=(long)mongoBufferConsumer.getBatchnumber();
            consumerStats.setBatchesConsumedPerIteration(iterationNo,batchesConsumedAfter-batchesConsumedBefore);
            if(producerScheduler.isRunningStatus()==false&& MongoBufferProducer.getBatchnumber()==MongoBufferConsumer.getBatchnumber()) {
                setRunningStatus(false);
            }
            }, 5, TimeUnit.SECONDS);
    }

    public boolean isRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(boolean runningStatus) {
        this.runningStatus = runningStatus;
    }

    public MongoBufferConsumer getMongoBufferConsumer() {
        return mongoBufferConsumer;
    }
}
