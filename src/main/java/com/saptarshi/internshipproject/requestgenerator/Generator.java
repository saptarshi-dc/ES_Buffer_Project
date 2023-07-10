package com.saptarshi.internshipproject.requestgenerator;

import com.saptarshi.internshipproject.model.Payload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Generator {
    private Random random;
    private int index;
    private List<Integer> rates;
    @Value("${partition.count}")
    private int numPartitions;
    private int nextPartition;

    public Generator() {
        random = new Random();
        index=-1;
        nextPartition=0;
        rates=new ArrayList<Integer>() {{
            add(50);
            add(25);
            add(12);
            add(44);
            add(65);
            add(82);
            add(10);
            add(36);
            add(97);
            add(78);
            add(49);
            add(75);
        }};
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
        return payload;
    }

    public int generateNewRate(){
        index++;
        if(index==rates.size())
            index=0;
        return rates.get(index);
    }
    public int generatePartitionNumber(int batchnumber){
        return batchnumber%numPartitions;
    }
}
