package com.saptarshi.internshipproject.model;

import org.springframework.data.annotation.Id;

public class Payload {
    @Id
    private Integer id;

    private String data;

    @Override
    public String toString() {
        return "Payload{" +
                "id=" + id +
                ", data='" + data + '\'' +
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
