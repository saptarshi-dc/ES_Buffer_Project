package com.saptarshi.internshipproject.service;

import com.saptarshi.internshipproject.perfstats.ChartGenerator;
import com.saptarshi.internshipproject.perfstats.ConsumerStats;
import com.saptarshi.internshipproject.perfstats.ProducerStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class MasterScheduler implements CommandLineRunner {
    @Value("${buffer.type}")
    private String bufferType;
    @Value("${consumers}")
    private int consumers;
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
    public void run(String... args) {
        if (bufferType.equals("mongodb"))
            mongoMasterScheduler();
        else if (bufferType.equals("kafka"))
            kafkaMasterScheduler();
        else
            LOGGER.info("Incorrect buffertype argument passed, please enter mongodb or kafka");
    }
    @DependsOn({"mongoTemplate","elasticsearchClient"})
    public void mongoMasterScheduler(){
        producerExecutorService = Executors.newSingleThreadScheduledExecutor();
        consumerExecutorService = Executors.newSingleThreadScheduledExecutor();
        shutdownExecutorService = Executors.newSingleThreadScheduledExecutor();
        producerTask = producerExecutorService.scheduleAtFixedRate(producerScheduler::updateRate, 0, 5000, TimeUnit.MILLISECONDS);
        consumerTask = consumerExecutorService.scheduleAtFixedRate(consumerScheduler::mongoConsumerScheduler, 0, 30000, TimeUnit.MILLISECONDS);

        shutdownExecutorService.schedule(() -> {
            System.out.println("Entered producer shutdown");
            if(!producerTask.isDone()){
                producerScheduler.setRunningStatus(false);
                producerScheduler.stopExecution();
                producerTask.cancel(false);
        }}, 120000, TimeUnit.MILLISECONDS);
    }
    @DependsOn({"kafkaTemplate","elasticsearchClient"})
    public void kafkaMasterScheduler(){
        producerExecutorService = Executors.newSingleThreadScheduledExecutor();
        consumerExecutorService = Executors.newSingleThreadScheduledExecutor();
        shutdownExecutorService = Executors.newSingleThreadScheduledExecutor();
        producerTask = producerExecutorService.scheduleAtFixedRate(producerScheduler::updateRate, 0, 5000, TimeUnit.MILLISECONDS);
        consumerTask = consumerExecutorService.scheduleAtFixedRate(consumerScheduler::kafkaConsumerScheduler, 0, 30000, TimeUnit.MILLISECONDS);

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

//            LOGGER.info("Time for each batch to be created:\n{}",producerStats.getBatchCreationTime().toString());
//            LOGGER.info("Time for each batch to enter buffer:\n{}",producerStats.getBufferBatchTime().toString());
//            LOGGER.info("Time for each batch to be read from buffer:\n{}",consumerStats.getBatchProcessingTime().toString());
//            LOGGER.info("Time for each batch to be indexed into elasticsearch:\n{}",consumerStats.getEsBatchTime().toString());
            LOGGER.info("Time for each batch from start to finish:\n{}",consumerStats.getBatchTotalTime().toString());

            chartGenerator.generateLineChart(producerStats.getBatchCreationTime(),"Producer: Batch Creation Time","Batch number","Time taken in Milliseconds");
            chartGenerator.generateLineChart(producerStats.getBufferBatchTime(),"Producer: Buffer Insertion Time","Batch number","Time taken in Milliseconds");
            chartGenerator.generateLineChart(producerStats.getBatchesCreatedPerInterval(),"Producer: Number of batches created in every 5 seconds interval","Interval number","Number of batches created");
            chartGenerator.generateLineChart(consumerStats.getBatchProcessingTime(),"Consumer: Batch Processing Time","Batch number","Time taken in Milliseconds");
            chartGenerator.generateLineChart(consumerStats.getEsBatchTime(),"Consumer: ElasticSearch Indexing Time","Batch number","Time taken in Milliseconds");
            chartGenerator.generateLineChart(consumerStats.getBatchesConsumedPerIteration(),"Consumer: Number of batches consumed per iteration of consumer process","Iteration number","Number of batches consumed");
            chartGenerator.generateLineChartWithVertical(consumerStats.getBatchTotalTime(),"Consumer - "+consumers+" threads: Time taken for each batch from creation to indexing","Producer Running Time","Time taken in Milliseconds",producerStats.getIntervals());

            long total=0;
            List<Long> intervals=producerStats.getIntervals();
            for(int i=0;i<intervals.size()-1;i++){
                if((intervals.get(i+1)-intervals.get(i))==0)
                    break;
                long totalTime=0L;
                long j=(i==0)?intervals.get(i):intervals.get(i)+1;
                for(;j<=intervals.get(i+1);j++){
                    Long batchTotalTime=consumerStats.getBatchTotalTime().get((int)j);
                    if(batchTotalTime!=null)
                        totalTime+=batchTotalTime;
                }
                if(i==0) {
                    total+=totalTime/(intervals.get(i+1)-intervals.get(i)+1);
                    consumerStats.setAvgTotalTimePerIteration(i+1,totalTime/(intervals.get(i+1)-intervals.get(i)+1));
                }
                else {
                    total+=totalTime/(intervals.get(i+1)-intervals.get(i));
                    consumerStats.setAvgTotalTimePerIteration(i + 1, totalTime / (intervals.get(i + 1) - intervals.get(i)));
                }
            }

            chartGenerator.generateLineChart(consumerStats.getAvgTotalTimePerIteration(),"Consumer - "+consumers+" threads:  Average total time taken per producer iteration","Producer iteration number","Time taken in Milliseconds");
            shutdown.stopApplication();
        }
    }
}
