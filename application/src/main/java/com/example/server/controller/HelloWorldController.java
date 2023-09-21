package com.example.server.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api")
@Log4j2
public class HelloWorldController {
    @GetMapping("/hello")
    public String sayHello() {
        log.info("Inside sayHello method!");
        return "Hello, World!";
    }

}
