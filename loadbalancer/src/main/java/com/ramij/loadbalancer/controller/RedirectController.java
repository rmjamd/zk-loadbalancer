package com.ramij.loadbalancer.controller;

import com.ramij.loadbalancer.annotations.GenerateRequestId;
import com.ramij.loadbalancer.service.LoadBalancerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController()
@RequestMapping("/api")
public class RedirectController {
	@Autowired
	LoadBalancerService loadBalancerService;
	@GenerateRequestId
	@GetMapping("/hello")
	public ResponseEntity <String> sayHello (HttpServletRequest request) {
		String id= (String) request.getAttribute("requestId");
		String targetUrl=loadBalancerService.getRedirectUrl(id);
		String response = redirectToTargetUrl(targetUrl);
		return new ResponseEntity <>(response,HttpStatus.OK);
	}


	private String redirectToTargetUrl (String targetUrl) {
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.getForObject(targetUrl, String.class);
	}


}
