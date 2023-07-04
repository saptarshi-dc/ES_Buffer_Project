package com.saptarshi.internshipproject.controller;

import com.saptarshi.internshipproject.service.ProducerScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProducerController {
    //    private final Generator generator;
    private final ProducerScheduler service;
    @Autowired
    public ProducerController(ProducerScheduler service) {
        this.service = service;
    }

//    @GetMapping("/producer")
//    @ResponseBody
//    public ResponseEntity<List<Payload>> docList(){
//        return service.getAllDocs();
//    }
}