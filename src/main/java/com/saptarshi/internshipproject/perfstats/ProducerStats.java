package com.saptarshi.internshipproject.perfstats;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ProducerStats {
    private Map<Integer,Long>bufferBatchTime;
    private Map<Integer,Long>batchCreationTime;
    private Map<Integer,Long>batchesCreatedPerMinute;
    private List<Long> intervals;
    public ProducerStats(){
        bufferBatchTime=new CustomMap<>();
        batchCreationTime=new CustomMap<>();
        batchesCreatedPerMinute=new CustomMap<>();
        intervals=new ArrayList<>();
        intervals.add(1L);
    }

    public Map<Integer, Long> getBufferBatchTime() {
        return bufferBatchTime;
    }

    public void setBufferBatchTime(Integer batchnumber, Long time) {
        bufferBatchTime.put(batchnumber,time);
    }

    public Map<Integer, Long> getBatchCreationTime() {
        return batchCreationTime;
    }

    public void setBatchCreationTime(Integer batchnumber, Long time) {
        batchCreationTime.put(batchnumber,time);
    }

    public Map<Integer, Long> getBatchesCreatedPerMinute() {
        return batchesCreatedPerMinute;
    }

    public void setBatchesCreatedPerMinute(Integer intervalNo, Long batchCount) {
        batchesCreatedPerMinute.put(intervalNo,batchCount);
    }

    public List<Long> getIntervals() {
        return intervals;
    }

    public void insertIntervals(Long batchnumber) {
        this.intervals.add(batchnumber);
    }
}
