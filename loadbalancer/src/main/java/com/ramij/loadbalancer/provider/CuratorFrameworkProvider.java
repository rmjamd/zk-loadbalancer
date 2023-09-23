package com.ramij.loadbalancer.provider;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CuratorFrameworkProvider {
	@Value("${zk.host}")
	private String zkHost;
	@Value("${zk.port}")
	private int    zkPort;


	@Bean(initMethod = "start",
		  destroyMethod = "close")
	public CuratorFramework createCuratorFramework () {
		return CuratorFrameworkFactory.newClient(
				zkHost + ":" + zkPort,
				new ExponentialBackoffRetry(1000, 3)
		);
	}

}
