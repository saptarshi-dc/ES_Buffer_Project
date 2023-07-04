package com.saptarshi.internshipproject.requestgenerator;

import com.saptarshi.internshipproject.model.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class Generator {
    private Random random;
    int index;
    List<Integer> rates;

    public Generator() {
        random = new Random();
        index=-1;
        rates=new ArrayList<Integer>() {{
            add(50);
            add(25);
            add(10);
            add(44);
            add(65);
            add(82);
            add(36);
            add(97);
            add(78);
            add(12);
            add(100);
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
//        return 60;
        index++;
        if(index==rates.size())
            index=0;
        return rates.get(index);
    }

}
