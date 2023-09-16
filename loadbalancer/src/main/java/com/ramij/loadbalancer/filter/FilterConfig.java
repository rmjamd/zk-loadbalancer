package com.ramij.loadbalancer.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {
	@Bean
	public FilterRegistrationBean<RequestIdFilter> requestIdFilterFilterRegistration(){
		FilterRegistrationBean<RequestIdFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new RequestIdFilter());
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE+1);
		registration.addUrlPatterns("/*"); // Define URL patterns to which the filter should be applied
		return registration;
	}

}
