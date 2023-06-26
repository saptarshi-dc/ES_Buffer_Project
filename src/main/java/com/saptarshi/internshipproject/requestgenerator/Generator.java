package com.saptarshi.internshipproject.requestgenerator;

import com.saptarshi.internshipproject.model.Payload;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

    public Payload generatePutRequest() {
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
        return payload;
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<Payload> entity = new HttpEntity<>(payload, headers);
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

//        requests.add(payload);

//        System.out.println("Sent message="+sb.toString());
//        System.out.println("Response body="+response.getBody());

    }

    public int generateNewRate(){
        return random.nextInt(6)+1;
    }

}
