package com.saptarshi.internshipproject.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName= "requests")
public class Payload {
    @Id
    @Field(type= FieldType.Integer)
    private Integer id;

    @Field(type=FieldType.Text)
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
