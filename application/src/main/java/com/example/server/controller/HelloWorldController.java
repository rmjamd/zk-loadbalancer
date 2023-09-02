package com.example.server.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api")
@Log4j2
public class HelloWorldController {
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World!";
    }

}
