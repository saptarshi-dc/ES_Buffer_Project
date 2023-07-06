//package com.saptarshi.internshipproject.bufferconsumer;
//
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.listener.ContainerProperties;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//
//@Configuration
//public class KafkaListenerConfig {
//
//    @Autowired
//    private KafkaProperties kafkaProperties;
//    @Value("${consumers}")
//    private int numInstances;
//
//    @Bean
//    public List<ConcurrentKafkaListenerContainerFactory<String, String>> kafkaListenerContainerFactories() {
//        List<ConcurrentKafkaListenerContainerFactory<String, String>> factories = new ArrayList<>(numInstances);
//
//        for (int i = 0; i < numInstances; i++) {
//            factories.add(createListenerContainerFactory("consumerGroup",""+i));
//        }
//
//        return factories;
//    }
//
//    private ConcurrentKafkaListenerContainerFactory<String, String> createListenerContainerFactory(String groupId,String consumerId) {
//        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory(groupId,consumerId));
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
//        return factory;
//    }
//
//    private ConsumerFactory<String, String> consumerFactory(String groupId,String consumerId) {
//        Map<String, Object> consumerProps = kafkaProperties.buildConsumerProperties();
//        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerId);
//        return new DefaultKafkaConsumerFactory<>(consumerProps);
//    }
//
//}
