package com.saptarshi.internshipproject.controller;

import com.saptarshi.internshipproject.model.Payload;
import com.saptarshi.internshipproject.requestgenerator.Generator;
import com.saptarshi.internshipproject.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProducerController {
//    private final Generator generator;
    private final ProducerService service;
    @Autowired
    public ProducerController(ProducerService service) {
        this.service = service;
    }

    @GetMapping("/producer")
    @ResponseBody
    public ResponseEntity<List<Payload>> docList(){
        return service.getAllDocs();
    }
}
