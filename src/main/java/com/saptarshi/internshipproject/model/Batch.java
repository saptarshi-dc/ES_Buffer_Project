package com.saptarshi.internshipproject.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class Batch {
    @Id
    private String id;
    private Integer batchnumber;
    private Integer size;
    private List<Payload> requests;
    private Long creationTime;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setRequests(List<Payload> requests) {
        this.requests = requests;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Integer getSize() {
        return size;
    }

    public Integer getBatchnumber() {
        return batchnumber;
    }

    public void setBatchnumber(Integer batchnumber) {
        this.batchnumber = batchnumber;
    }

    public List<Payload> getRequests() {
        return requests;
    }

}
