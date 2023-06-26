package com.saptarshi.internshipprojectmdb.bufferconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.saptarshi.internshipprojectmdb.model.Batch;
import com.saptarshi.internshipprojectmdb.model.Payload;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class KafkaBufferConsumer implements BufferConsumer {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RestHighLevelClient client;
    private int total=0;
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Logger LOGGER= LoggerFactory.getLogger(MongoBufferConsumer.class);

    public int getTotal() {
        return total;
    }

    public void consume() {
        while (true) {
            Query query = new Query();
            query.with(Sort.by("$natural").ascending());
            Batch batch = mongoTemplate.findOne(query, Batch.class);
            if (batch == null)
                return;

            BulkRequest br = new BulkRequest();
            for (Payload request : batch.getRequests()) {
                try {
                    br.add(new IndexRequest("requests_index")
                            .id(String.valueOf(request.getId()))
                            .source(objectMapper.writeValueAsString(request), XContentType.JSON));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            try {
                BulkResponse bulkResponse = client.bulk(br, RequestOptions.DEFAULT);
                total += batch.getSize();
                mongoTemplate.remove(batch);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}