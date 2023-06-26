package com.saptarshi.internshipprojectmdb.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection="requestcollection2")
public class Batch {
    @Id
    private String id;
    private Integer size;
    private List<Payload> requests;
    private Instant bufferInsertionTime;

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

    public Instant getBufferInsertionTime() {
        return bufferInsertionTime;
    }

    public void setBufferInsertionTime(Instant bufferInsertionTime) {
        this.bufferInsertionTime = bufferInsertionTime;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<Payload> getRequests() {
        return requests;
    }

    public void setBatch(List<Payload>requests) {
        this.requests = requests;
    }
}
