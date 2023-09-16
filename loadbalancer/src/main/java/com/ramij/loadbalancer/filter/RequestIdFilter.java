package com.ramij.loadbalancer.filter;

import com.ramij.loadbalancer.annotations.GenerateRequestId;
import jakarta.servlet.*;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.UUID;

@WebFilter("/*")
public class RequestIdFilter implements Filter {
	private RequestMappingHandlerMapping handlerMapping;


	@Override
	public void init (FilterConfig filterConfig) throws ServletException {
		// Obtain the Spring WebApplicationContext to access the RequestMappingHandlerMapping
		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
		if (context != null) {
			handlerMapping = context.getBean(RequestMappingHandlerMapping.class);
		}
	}


	@Override
	public void doFilter (ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (handlerMapping != null) {
			HandlerExecutionChain executionChain = null;
			try {
				executionChain = handlerMapping.getHandler((HttpServletRequest) request);
				if (executionChain != null) {
					Object        handler       = executionChain.getHandler();
					HandlerMethod handlerMethod = (HandlerMethod) handler;
					// Check for the method-level annotation
					if (handlerMethod.getMethod().isAnnotationPresent(GenerateRequestId.class)) {
						String requestId = UUID.randomUUID().toString();
						request.setAttribute("requestId", requestId);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		chain.doFilter(request, response);
	}


	@Override
	public void destroy () {
		Filter.super.destroy();
	}
}
