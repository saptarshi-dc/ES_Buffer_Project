package com.saptarshi.internshipprojectmdb.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


//@Document
public class Payload {
    @Id
    private Integer id;

    private String data;
    private Instant creationTime;
    private Instant bufferInsertionTime;
    private Instant esInsertionTime;

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

    public Instant getBufferInsertionTime() {
        return bufferInsertionTime;
    }

    public void setBufferInsertionTime(Instant bufferInsertionTime) {
        this.bufferInsertionTime = bufferInsertionTime;
    }

    public Instant getEsInsertionTime() {
        return esInsertionTime;
    }

    public void setEsInsertionTime(Instant esInsertionTime) {
        this.esInsertionTime = esInsertionTime;
    }

    @Override
    public String toString() {
        return "Payload{" +
                "id=" + id +
                ", data='" + data + '\'' +
                ", creationTime=" + creationTime +
                ", bufferInsertionTime=" + bufferInsertionTime +
                ", esInsertionTime=" + esInsertionTime +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
