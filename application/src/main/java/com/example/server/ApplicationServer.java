package com.example.server;

import com.example.server.provider.ServerInfoProvider;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Log4j2
public class ApplicationServer {
	private final CuratorFramework curatorFramework;
	private final ServerInfoProvider serverInfoProvider;
	private       String           nodePath;

	public static final String ROOT_NODE_PATH = "/loadbalancer";
	public static final String FORWARD_SLASH  = "/";


	@Autowired
	public ApplicationServer (CuratorFramework curatorFramework, ServerInfoProvider serverInfoProvider, ServerInfoProvider serverInfoProvider1) {

		this.curatorFramework = curatorFramework;
		this.serverInfoProvider = serverInfoProvider1;
	}


	public static void main (String[] args) {
		SpringApplication.run(ApplicationServer.class, args);
	}


	@PostConstruct
	public void createZooKeeperNode () {
		checkIfParentNodeExist();
		createChildNode();
	}


	private void createChildNode () {
		String nodeData = serverInfoProvider.getIpAddress();
		try {
			// Create an EPHEMERAL_SEQUENTIAL node under the root node
			nodePath = curatorFramework.create()
									   .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
									   .forPath(ROOT_NODE_PATH + FORWARD_SLASH, nodeData.getBytes());

			log.info("Started with child node path: " + nodePath);

		} catch (Exception e) {
			log.error("Failed to Create Node under Zookeeper");
			log.error(e);
			shutDownApplication();
		}
	}


	private void checkIfParentNodeExist () {
		curatorFramework.start();
		// Create the root node if it doesn't exist
		try {
			if (curatorFramework.checkExists().forPath(ROOT_NODE_PATH) == null) {
				curatorFramework.create().creatingParentsIfNeeded().forPath(ROOT_NODE_PATH);
			}
		} catch (Exception e) {
			log.error("Failed to create root Node");
			log.error(e);
			shutDownApplication();
		}
	}


	public void shutDownApplication () {
		System.exit(1);
	}


	@PreDestroy
	public void removeZooKeeperNode () {
		try {
			curatorFramework.delete().deletingChildrenIfNeeded().forPath(nodePath);
		} catch (Exception e) {
			log.error("Failed to Create");
		}
	}
}
