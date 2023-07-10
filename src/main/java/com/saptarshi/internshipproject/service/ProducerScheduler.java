package com.saptarshi.internshipproject.service;

import com.saptarshi.internshipproject.bufferproducer.KafkaBufferProducer;
import com.saptarshi.internshipproject.bufferproducer.MongoBufferProducer;
import com.saptarshi.internshipproject.perfstats.ProducerStats;
import com.saptarshi.internshipproject.requestgenerator.Generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ProducerScheduler {
    @Value("${batch.size}")
    private int batchSize;
    @Value("${buffer.type}")
    private String bufferType;
    private MongoBufferProducer mongoBufferProducer;
    private KafkaBufferProducer kafkaBufferProducer;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> producerTask;
    @Autowired
    private ProducerStats producerStats;
    @Autowired
    private Generator generator;
    private int rate;
    private boolean runningStatus;
    private Integer intervalNo;
    private Long batchCountBefore;
    private Long batchCountAfter;
    private static final Logger LOGGER= LoggerFactory.getLogger(ProducerScheduler.class);
    public void updateRate() {
        stopExecution();
        rate = generator.generateNewRate();
        System.out.println("New rate=" + (60000 / (rate*10))*batchSize + " documents per minute");
        startExecution();
    }

    @Autowired
    public ProducerScheduler(MongoBufferProducer mongoBufferProducer,KafkaBufferProducer kafkaBufferProducer) {
        this.mongoBufferProducer=mongoBufferProducer;
        this.kafkaBufferProducer=kafkaBufferProducer;
        executorService = Executors.newSingleThreadScheduledExecutor();
        batchCountBefore=0L;
        batchCountAfter=0L;
        rate = 100;
        runningStatus=true;
        intervalNo=0;
    }

    public void startExecution() {
        if(isRunningStatus()==false)
            return;
        intervalNo++;
        if(bufferType.equals("mongodb"))
        {
            batchCountBefore=(long)mongoBufferProducer.getBatchnumber();
            producerTask = executorService.scheduleAtFixedRate(mongoBufferProducer::produce, 0, rate*10, TimeUnit.MILLISECONDS);
        }
        else {
            batchCountBefore=(long)kafkaBufferProducer.getBatchnumber();
            producerTask = executorService.scheduleAtFixedRate(kafkaBufferProducer::produce, 0, rate*10, TimeUnit.MILLISECONDS);
        }
    }

    public void stopExecution() {
        if (producerTask!=null) {
            LOGGER.info("Trying to shut down producer");
            try {
                producerTask.cancel(false);
                if (bufferType.equals("mongodb"))
                    batchCountAfter = (long) mongoBufferProducer.getBatchnumber();
                else
                    batchCountAfter = (long) kafkaBufferProducer.getBatchnumber();
                producerStats.insertIntervals(batchCountAfter);
                producerStats.setBatchesCreatedPerInterval(intervalNo, batchCountAfter - batchCountBefore);
            } catch (Exception e) {
            }
        }
    }

    public boolean isRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(boolean runningStatus) {
        this.runningStatus = runningStatus;
    }
}
