package com.saptarshi.internshipprojectmdb.requestgenerator;

import com.saptarshi.internshipprojectmdb.model.Payload;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class Generator {
    private RestTemplate restTemplate;
    private Random random;

    public Generator() {
        restTemplate = new RestTemplate();
        random = new Random();
    }

    public Payload generatePayload() {
        Payload payload=new Payload();
        StringBuilder sb=new StringBuilder();

        String chars="ABCDEFGHIJKLMNOPQRSTUVWXYZ ,.?!:;";
        Random random=new Random();
        int len=random.nextInt(500);

        for(int i=0;i<len;i++){
            int pos= random.nextInt(chars.length());
            sb.append(chars.charAt(pos));
        }
        payload.setId(random.nextInt((int)1e9));
        payload.setData(sb.toString());
        Instant currentTime=Instant.now();
        payload.setCreationTime(currentTime);
//        payload.setBufferInsertionTime(currentTime);
//        payload.setEsInsertionTime(currentTime);
        return payload;
    }

    public int generateNewRate(){
//        return 60;
        return random.nextInt(10)+1;
    }

}
