package com.ramij.loadbalancer.service;

import com.ramij.hashing.ConsistentHashBuilder;
import com.ramij.hashing.ConsistentHashing;
import com.ramij.hashing.nodes.Node;
import com.ramij.hashing.nodes.ServerNode;
import com.ramij.loadbalancer.constants.Constant;
import com.ramij.loadbalancer.exceptions.ApplicationNodeRetrievalException;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Log4j2
public class LoadBalancerServiceImpl implements LoadBalancerService {
	@Value("${zk.host}")
	private String zookeeperHost;
	@Value("${zk.port}")
	private int    zookeeperPort;
	@Value("${zk.node.path}")
	private String zookeeperNodePath;

	@Value("${server.replicas}")
	private int noOfReplicas;

	ConsistentHashing <Node> consistentHashing;
	private final ExecutorService executorService = Executors.newFixedThreadPool(1);

	CuratorFramework curatorFramework;


	@Override
	public String getRedirectUrl (String requestId) {
		Node node = consistentHashing.getNode(requestId);
		if (node != null) {
			return String.format(Constant.REDIRECT_URL_FORMAT, node.getKey());
		} else {
			return null;
		}
	}


	@PostConstruct
	public void init () {
		consistentHashing = ConsistentHashBuilder.create().addReplicas(noOfReplicas).build();
		initializeCuratorFramework();
		addChildNode(curatorFramework);
		addNodeEventListener();
	}


	private void initializeCuratorFramework () {
		curatorFramework  = CuratorFrameworkFactory.newClient(zookeeperHost + ":" + zookeeperPort,
				new ExponentialBackoffRetry(1000, 3));
		curatorFramework.start();
	}


	private void addNodeEventListener () {
		executorService.submit(() -> {
			CuratorCache curatorCache = CuratorCache.build(curatorFramework, zookeeperNodePath);
			curatorCache.start();

			CuratorCacheListener listener = CuratorCacheListener.builder()
																.forPathChildrenCache(zookeeperNodePath, curatorFramework, (client, event) -> {
																	log.info("Child event received");
																	ChildData                   data = event.getData();
																	PathChildrenCacheEvent.Type type = event.getType();
																	if (Objects.requireNonNull(type) == PathChildrenCacheEvent.Type.CHILD_ADDED) {
																		String nodeData = new String(data.getData());
																		log.info("Child node added: " + nodeData);
																		addNodeToConsistentHashing(nodeData);
																	} else if (type == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
																		log.info("Child node removed");
																		String nodeData = new String(data.getData());
																		removeNodeFromConsistentHashing(nodeData);
																	}
																})
																.build();

			curatorCache.listenable().addListener(listener);
		});
	}


	private void addChildNode (CuratorFramework curatorFramework) {
		try {
			List <String> children = curatorFramework.getChildren().forPath(zookeeperNodePath);
			for (String child : children) {
				String childNodePath = ZKPaths.makePath(zookeeperNodePath, child);
				byte[] bytes         = curatorFramework.getData().forPath(childNodePath);
				Stat   stat          = curatorFramework.checkExists().forPath(childNodePath);
				if (stat != null) {
					String data = new String(bytes, StandardCharsets.UTF_8);
					consistentHashing.addNode(extractServerNode(data));
				}

			}
		} catch (Exception e) {
			log.error("Error in Retrieving childNode from zookeeper");
			throw new ApplicationNodeRetrievalException(e.getMessage());
		}
	}


	private void removeNodeFromConsistentHashing (String nodeData) {
		consistentHashing.removeNode(extractServerNode(nodeData));
	}


	private void addNodeToConsistentHashing (String nodeData) {
		consistentHashing.addNode(extractServerNode(nodeData));
	}


	private ServerNode extractServerNode (String nodeData) {
		String[] parts = nodeData.split(":");
		String   host  = parts[0];
		String   port  = parts[1];
		return new ServerNode(host, Integer.parseInt(port));


	}

}

