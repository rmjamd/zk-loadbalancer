package com.ramij.loadbalancer.exceptions;

public class ApplicationNodeRetrievalException extends RuntimeException {

	public ApplicationNodeRetrievalException (String message) {
		super(message);
	}

	public ApplicationNodeRetrievalException (String message, Throwable cause) {
		super(message, cause);
	}
}
