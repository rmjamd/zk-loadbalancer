package com.example.server.controller;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CuratorConfig {
	@Value("${zookeeper.connectionString}")
	private String zookeeperConnectionString;


	@Bean
	public CuratorFramework curatorFramework () {
		return CuratorFrameworkFactory.builder()
									  .connectString(zookeeperConnectionString)
									  .retryPolicy(new ExponentialBackoffRetry(1000, 3))
									  .build();
	}
}
