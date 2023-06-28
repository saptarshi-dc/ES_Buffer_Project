package com.saptarshi.internshipprojectmdb.perfstats;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProducerStats {
    private Map<Integer,Long>mongoBatchTime;
    private Map<Integer,Long>batchCreationTime;
    private Map<Integer,Long>batchesCreatedPerMinute;
    public ProducerStats(){
        mongoBatchTime=new CustomMap<>();
        batchCreationTime=new CustomMap<>();
        batchesCreatedPerMinute=new CustomMap<>();
    }

    public Map<Integer, Long> getMongoBatchTime() {
        return mongoBatchTime;
    }

    public void setMongoBatchTime(Integer batchnumber, Long time) {
        mongoBatchTime.put(batchnumber,time);
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
}
