package com.bridgelabz.todo.userservice.exception;

public class EmailIdAlreadyExistException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	private String errorMessage;
	 
    public EmailIdAlreadyExistException() {
	}
 
    public EmailIdAlreadyExistException(String errorMessage) {
        this.errorMessage = errorMessage;
    }
 
    public String getErrorMessage() {
        return errorMessage;
    }

}
