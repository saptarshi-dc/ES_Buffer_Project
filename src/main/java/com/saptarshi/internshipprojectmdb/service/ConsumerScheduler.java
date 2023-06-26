package com.saptarshi.internshipprojectmdb.service;

import com.saptarshi.internshipprojectmdb.bufferconsumer.MongoBufferConsumer;
import com.saptarshi.internshipprojectmdb.bufferproducer.MongoBufferProducer;
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
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> consumerTask;
    @Autowired
    private ProducerScheduler producerScheduler;
    private boolean runningStatus;
    @Value("${batch.size}")
    private int batchsize;
    private static final Logger LOGGER=LoggerFactory.getLogger(ConsumerScheduler.class);

    @Autowired
    public ConsumerScheduler(MongoBufferConsumer mongoBufferConsumer,Shutdown shutdown) {
        this.mongoBufferConsumer=mongoBufferConsumer;
        executorService = Executors.newSingleThreadScheduledExecutor();
        runningStatus=true;
    }
//    @Scheduled(fixedRate = 4000)
    public void scheduler(){
        if(isRunningStatus()==false)
            return;
        consumerTask=executorService.schedule(mongoBufferConsumer::consume,0,TimeUnit.SECONDS);
        executorService.schedule(() -> {
            if (!consumerTask.isDone()) {
                consumerTask.cancel(true);
            }
        }, 1, TimeUnit.SECONDS);
        LOGGER.info(String.format("Documents indexed in elasticsearch=%d",mongoBufferConsumer.getBatchnumber()*batchsize));

        if(producerScheduler.isRunningStatus()==false&& MongoBufferProducer.getBatchnumber()==MongoBufferConsumer.getBatchnumber()) {
            setRunningStatus(false);
        }
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
