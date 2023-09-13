package com.ramij.loadbalancer.controller;

import com.ramij.hashing.ConsistentHashBuilder;
import com.ramij.loadbalancer.constants.Constant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController("/api")
public class RedirectController {
	@GetMapping("/hello")
	public ResponseEntity <Void> sayHello () {
		String       ip           = "localhost:8080"; //ToDo : get Ip from constient hashing,
//		ConsistentHashBuilder.create().addReplicas(3).build();
		String       targetUrl    = String.format(Constant.REDIRECT_URL_FORMAT, ip);
		RestTemplate restTemplate = new RestTemplate();

		// Make an HTTP GET request to the target URL
		restTemplate.getForObject(targetUrl, String.class);

		// Return a 302 Found response to instruct the client to redirect
		return new ResponseEntity <>(HttpStatus.FOUND);
	}

}
