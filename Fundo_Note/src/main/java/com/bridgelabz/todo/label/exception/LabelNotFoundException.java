package com.bridgelabz.todo.label.exception;

public class LabelNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String errorMessage;
	
	public LabelNotFoundException(String errorMessage) {
        super(errorMessage);

		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	
}
