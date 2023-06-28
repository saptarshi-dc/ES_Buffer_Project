package com.saptarshi.internshipprojectmdb.perfstats;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConsumerStats {
    private Map<Integer,Long> esBatchTime;
    private Map<Integer,Long> batchProcessingTime;
    private Map<Integer,Long>batchesConsumedPerIteration;

    public ConsumerStats(){
        esBatchTime=new CustomMap<>();
        batchProcessingTime=new CustomMap<>();
        batchesConsumedPerIteration=new CustomMap<>();
    }

    public Map<Integer, Long> getEsBatchTime() {
        return esBatchTime;
    }

    public void setEsBatchTime(Integer batchnumber, Long time) {
        esBatchTime.put(batchnumber,time);
    }

    public Map<Integer, Long> getBatchProcessingTime() {
        return batchProcessingTime;
    }

    public void setBatchProcessingTime(Integer batchnumber, Long time) {
        batchProcessingTime.put(batchnumber,time);
    }

    public Map<Integer, Long> getBatchesConsumedPerIteration() {
        return batchesConsumedPerIteration;
    }

    public void setBatchesConsumedPerIteration(Integer iterationNo, Long batchCount) {
        batchesConsumedPerIteration.put(iterationNo,batchCount);
    }
}
