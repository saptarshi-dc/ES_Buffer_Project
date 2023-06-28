package com.saptarshi.internshipprojectmdb.service;

import com.saptarshi.internshipprojectmdb.bufferconsumer.MongoBufferConsumer;
import com.saptarshi.internshipprojectmdb.bufferproducer.MongoBufferProducer;
import com.saptarshi.internshipprojectmdb.perfstats.ChartGenerator;
import com.saptarshi.internshipprojectmdb.perfstats.ConsumerStats;
import com.saptarshi.internshipprojectmdb.perfstats.ProducerStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jfree.chart.ChartUtils;

@Service
public class MasterScheduler implements CommandLineRunner {
    @Autowired
    private ProducerScheduler producerScheduler;
    @Autowired
    private Shutdown shutdown;
    @Autowired
    private ConsumerScheduler consumerScheduler;
    private ScheduledExecutorService producerExecutorService;
    private ScheduledExecutorService consumerExecutorService;
    private ScheduledExecutorService shutdownExecutorService;
    private ScheduledFuture<?> producerTask;
    private ScheduledFuture<?> consumerTask;
    @Autowired
    private ProducerStats producerStats;
    @Autowired
    private ConsumerStats consumerStats;
    @Autowired
    private ChartGenerator chartGenerator;
    private static final Logger LOGGER= LoggerFactory.getLogger(MasterScheduler.class);

    @Override
    @DependsOn({"mongoTemplate","elasticsearchClient"})
    public void run(String... args) {
        producerExecutorService = Executors.newSingleThreadScheduledExecutor();
        consumerExecutorService = Executors.newSingleThreadScheduledExecutor();
        shutdownExecutorService = Executors.newSingleThreadScheduledExecutor();
        producerTask = producerExecutorService.scheduleAtFixedRate(producerScheduler::updateRate, 0, 5000, TimeUnit.MILLISECONDS);
        consumerTask = consumerExecutorService.scheduleAtFixedRate(consumerScheduler::scheduler, 5000, 10000, TimeUnit.MILLISECONDS);

        shutdownExecutorService.schedule(() -> {
            System.out.println("Entered producer shutdown");
            if(!producerTask.isDone()){
                producerScheduler.setRunningStatus(false);
                producerScheduler.stopExecution();
                producerTask.cancel(false);
        }}, 120000, TimeUnit.MILLISECONDS);
    }

    @Scheduled(initialDelay=120000,fixedRate = 1000)
    public void stopExecution() {
        if (consumerScheduler.isRunningStatus()==false) {
            consumerTask.cancel(false);
            LOGGER.info("Time for each batch to be created:\n{}",producerStats.getBatchCreationTime().toString());
            LOGGER.info("Time for each batch to enter mongodb:\n{}",producerStats.getMongoBatchTime().toString());
//            LOGGER.info("Average time for 1 batch from start to finish={}",MongoBufferConsumer.getTotalBatchIndexingTime().dividedBy(MongoBufferConsumer.getBatchnumber()));
            LOGGER.info("Time for each batch to be read from mongodb:\n{}",consumerStats.getBatchProcessingTime().toString());
            LOGGER.info("Time for each batch to be indexed into elasticsearch:\n{}",consumerStats.getEsBatchTime().toString());
//            LOGGER.info("Number of batches consumed per iteration:\n{}",consumerStats.getBatchesConsumedPerIteration().toString());

            chartGenerator.generateLineChart(producerStats.getBatchCreationTime(),"Producer: Batch Creation Time","Batch number","Time in Milliseconds");
            chartGenerator.generateLineChart(producerStats.getMongoBatchTime(),"Producer: MongoDB Buffer Insertion Time","Batch number","Time in Milliseconds");
            chartGenerator.generateLineChart(producerStats.getBatchesCreatedPerMinute(),"Producer: Number of batches created in every 5 seconds interval","Interval number","Number of batches created");
            chartGenerator.generateLineChart(consumerStats.getBatchProcessingTime(),"Consumer: Batch Processing Time","Batch number","Time in Milliseconds");
            chartGenerator.generateLineChart(consumerStats.getEsBatchTime(),"Consumer: ElasticSearch Indexing Time","Batch number","Time in Milliseconds");
            chartGenerator.generateLineChart(consumerStats.getBatchesConsumedPerIteration(),"Consumer: Number of batches consumed per iteration of consumer process","Iteration number","Number of batches consumed");

            shutdown.stopApplication();
        }
    }
}
