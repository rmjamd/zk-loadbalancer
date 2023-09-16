package com.ramij.loadbalancer.controller;

import com.ramij.hashing.ConsistentHashBuilder;
import com.ramij.hashing.ConsistentHashing;
import com.ramij.hashing.nodes.Node;
import com.ramij.hashing.nodes.ServerNode;
import com.ramij.loadbalancer.annotations.GenerateRequestId;
import com.ramij.loadbalancer.constants.Constant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController()
@RequestMapping("/api")
public class RedirectController {
	@GenerateRequestId
	@GetMapping("/hello")
	public ResponseEntity <String> sayHello (HttpServletRequest request) {
		String id= (String) request.getAttribute("requestId");
		ConsistentHashing <Node> consistentHashing = ConsistentHashBuilder.create().addReplicas(3).addNode(new ServerNode("localhost",5050)).build();
		String                   targetUrl         = String.format(Constant.REDIRECT_URL_FORMAT, consistentHashing.getNode(id));
		RestTemplate restTemplate = new RestTemplate();
		// Make an HTTP GET request to the target URL
		String response = restTemplate.getForObject(targetUrl, String.class);
		// Return a 302 Found response to instruct the client to redirect
		return new ResponseEntity <>(response,HttpStatus.OK);
	}

}
