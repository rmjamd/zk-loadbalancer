package com.ramij.loadbalancer.provider;

import com.ramij.hashing.ConsistentHashBuilder;
import com.ramij.hashing.ConsistentHashing;
import com.ramij.hashing.nodes.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsistentHashingProvider {
	@Value("${server.replicas}")
	int noOfReplicas;


	@Bean
	public ConsistentHashing <Node> getConsistentHashing () {
		return ConsistentHashBuilder.create().addReplicas(noOfReplicas).build();
	}
}
