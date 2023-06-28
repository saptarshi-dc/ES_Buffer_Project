package com.saptarshi.internshipprojectmdb.perfstats;

import java.util.HashMap;
import java.util.Map;

public class CustomMap<K,V> extends HashMap<K,V> {
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append("{");
        for (Map.Entry<K, V> entry : this.entrySet()) {
            sb.append("Batch no. ").append(entry.getKey()).append("=").append(entry.getValue()).append(" ms\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
