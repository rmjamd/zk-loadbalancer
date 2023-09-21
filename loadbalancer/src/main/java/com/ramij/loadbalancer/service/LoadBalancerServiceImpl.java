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
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Component
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
		try (CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(zookeeperHost + ":" + zookeeperPort,
				new ExponentialBackoffRetry(1000, 3))) {
			curatorFramework.start();
			addChildNode(curatorFramework);
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
			throw new RuntimeException(e);
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
		String host = parts[0];
		String port = parts[1];
		return new ServerNode(host, Integer.parseInt(port));


	}
}

