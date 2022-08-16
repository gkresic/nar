package com.steatoda.nar.service.crud;

public class NarCRUDException extends RuntimeException {

	public NarCRUDException(String message) {
		super(message);
	}

	public NarCRUDException(String message, Exception cause) {
		super(message, cause);
	}

	private static final long serialVersionUID = 1L;

}
