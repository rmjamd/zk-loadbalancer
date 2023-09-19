package com.ramij.loadbalancer.service;

public interface LoadBalancerService {
	String getRedirectUrl(String requestId);
}
