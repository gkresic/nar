package com.steatoda.nar.service.crud;

public class CRUDException extends RuntimeException {

	public CRUDException(String message) {
		super(message);
	}

	public CRUDException(String message, Exception cause) {
		super(message, cause);
	}

	private static final long serialVersionUID = 1L;

}
