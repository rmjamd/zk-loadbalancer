package com.ramij.loadbalancer.service;

import com.ramij.hashing.ConsistentHashBuilder;
import com.ramij.hashing.ConsistentHashing;
import com.ramij.hashing.nodes.Node;
import com.ramij.hashing.nodes.ServerNode;
import com.ramij.loadbalancer.constants.Constant;
import jakarta.annotation.PostConstruct;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;

public class LoadBalancerServiceImpl implements LoadBalancerService {
	@Value("${zk.host}")
	private String zookeeperHost;
	@Value("${zk.port}")
	private int    zookeeperPort;
	@Value("${zk.node.path}")
	private String zookeeperNodePath;

	@Value("${server.replicas}")
	private int noOfReplicas;


	@Override
	public String getRedirectUrl (String requestId) {
		Node node = consistentHashing.getNode(requestId);
		if (node != null) {
			return String.format(Constant.REDIRECT_URL_FORMAT, node.getKey());
		} else {
			return null;
		}
	}


	ConsistentHashing <Node> consistentHashing;


	@PostConstruct
	public void init () {
		consistentHashing = ConsistentHashBuilder.create().addReplicas(noOfReplicas).build();
		try (CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(zookeeperHost + zookeeperPort,
				new ExponentialBackoffRetry(1000, 3))) {
			curatorFramework.start();
			PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, zookeeperNodePath, true);
			pathChildrenCache.getListenable().addListener((client, event) -> {
				String nodeData = new String(event.getData().getData());
				if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
					addNodeToConsistentHashing(nodeData);
				} else if (event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
					removeNodeFromConsistentHashing(nodeData);
				}
			});
		}
	}


	private void removeNodeFromConsistentHashing (String nodeData) {
		consistentHashing.removeNode(extractServerNode(nodeData));
	}


	private void addNodeToConsistentHashing (String nodeData) {
		consistentHashing.addNode(extractServerNode(nodeData));
	}


	private ServerNode extractServerNode (String nodeData) {
		int    ind  = nodeData.indexOf(':');
		String host = nodeData.substring(ind - 1);
		int    port = Integer.parseInt(nodeData.substring(ind + 1, nodeData.length() - 1));
		return new ServerNode(host, port);


	}
}

