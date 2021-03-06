package com.bridgelabz.todo.userservice.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.bridgelabz.todo.userservice.model.EmailModel;
import com.bridgelabz.todo.userservice.model.RegisterModel;
import com.bridgelabz.todo.userservice.model.User;

public interface IUserService {

	void insert(RegisterModel registerModel,HttpServletRequest request);

	boolean isUserExist(String email);
	
	User getUserDetailsByEmail(String email);
	
	User getUserById(long id);
	 
	void updateUser(User user);

	EmailModel getEmailModel(String token,HttpServletRequest request,User user);

	void isVerifiedUser(String token) ;

	String forgotPassword(String email,HttpServletRequest request);

	void restPassword(String token,String newPassword);

	String verifyLogin(String email,String password);

	User getCurrentUser(String token);

	void update(User user, String token);

	List<?> getAllUsers(String token);

}