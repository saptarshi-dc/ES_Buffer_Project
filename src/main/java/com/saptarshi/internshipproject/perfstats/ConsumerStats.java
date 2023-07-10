package com.saptarshi.internshipproject.perfstats;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConsumerStats {
    private Map<Integer,Long> esBatchTime;
    private Map<Integer,Long> batchProcessingTime;
    private Map<Integer,Long>batchesConsumedPerIteration;
    private Map<Integer,Long>batchTotalTime;
    private Map<Integer,Long>avgTotalTimePerIteration;

    public ConsumerStats(){
        esBatchTime=new CustomMap<>();
        batchProcessingTime=new CustomMap<>();
        batchesConsumedPerIteration=new CustomMap<>();
        batchTotalTime=new CustomMap<>();
        avgTotalTimePerIteration=new CustomMap<>();
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

    public Map<Integer, Long> getBatchTotalTime() {
        return batchTotalTime;
    }

    public void setBatchTotalTime(Integer batchnumber, Long time) {
        batchTotalTime.put(batchnumber,time);
    }

    public Map<Integer, Long> getAvgTotalTimePerIteration() {
        return avgTotalTimePerIteration;
    }

    public void setAvgTotalTimePerIteration(Integer iterationNo, Long time) {
        avgTotalTimePerIteration.put(iterationNo,time);
    }
}
