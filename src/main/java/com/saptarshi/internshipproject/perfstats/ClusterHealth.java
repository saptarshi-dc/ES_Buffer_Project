package com.saptarshi.internshipproject.perfstats;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsRequest;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.action.admin.indices.stats.CommonStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.health.ClusterIndexHealth;
import org.elasticsearch.cluster.health.ClusterIndexHealth;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
//import org.elasticsearch.cluster.ClusterStats;
//import org.elasticsearch.cluster.stats.ClusterIndexingStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

//@Service
//public class ClusterHealth{
//    @Autowired
//    private RestHighLevelClient client;
//    @Scheduled(fixedRate = 1000,timeUnit = TimeUnit.MILLISECONDS)
//    public void writeQueueSize() {
//        ClusterHealthRequest request = new ClusterHealthRequest();
//        try {
//            ClusterHealthResponse response = client.cluster().health(request, RequestOptions.DEFAULT);
//            System.out.println("Pending tasks = "+response.getNumberOfPendingTasks());
//        }catch(Exception e){
//        }
//    }
//}