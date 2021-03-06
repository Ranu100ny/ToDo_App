package com.bridgelabz.todo.userservice.service;

import java.security.SignatureException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bridgelabz.todo.noteservice.exception.UnauthorizedException;
import com.bridgelabz.todo.userservice.dao.IUserDao;
import com.bridgelabz.todo.userservice.exception.TokenExpireException;
import com.bridgelabz.todo.userservice.exception.UserNotFoundException;
import com.bridgelabz.todo.userservice.jms.MessageSender;
import com.bridgelabz.todo.userservice.model.EmailModel;
import com.bridgelabz.todo.userservice.model.RegisterModel;
import com.bridgelabz.todo.userservice.model.User;
import com.bridgelabz.todo.utility.RedisUtility;
import com.bridgelabz.todo.utility.Token;

@Service
public class UserServiceImpl implements IUserService {

	@Autowired
	private IUserDao userDao;

	@Autowired
	private EmailModel emailModel;

	@Autowired
	private MessageSender messageSender;

	@Autowired
	private RedisUtility redisUtility;

	@Transactional
	@Override
	public void insert(RegisterModel registerModel, HttpServletRequest request) {

		User user = new User(registerModel);
		user.setPassword(BCrypt.hashpw(registerModel.getPassword(), BCrypt.gensalt(12)));
		userDao.insert(user);

		String token = Token.generateToken(user.getId());

		StringBuffer URL = request.getRequestURL();

		String url = "<a href=" + URL.substring(0, URL.lastIndexOf("/")) + "/verifytoken/" + token + " ></a>";

		
		String subject = "link to activate your account";

		emailModel.setSubject(subject);
		emailModel.setTo(user.getEmail());
		emailModel.setUrl(url);

		messageSender.sendMessage(emailModel);

		redisUtility.saveToken(Long.toString(user.getId()), token);

	}

	@Transactional
	@Override
	public boolean isUserExist(String email) {
		long count = userDao.isUserExist(email);

		if (count >= 1) {
			return true;
		} else {
			return false;
		}

	}

	@Transactional
	@Override
	public User getUserDetailsByEmail(String email) {
		return userDao.getUserDetailsByEmail(email);
	}

	@Transactional
	@Override
	public User getUserById(long id) {
		return userDao.getUserById(id);
	}

	@Transactional
	@Override
	public void updateUser(User user) {
		userDao.updateUser(user);
	}

	@Transactional
	@Override
	public EmailModel getEmailModel(String token, HttpServletRequest request, User user) {
		StringBuffer URL = request.getRequestURL();

		String url = "<a href=" + URL.substring(0, URL.lastIndexOf("/")) + "/resetpassword/" + token + " ></a>";

		String subject = "link to rest your password";

		emailModel.setTo(user.getEmail());
		emailModel.setSubject(subject);
		emailModel.setUrl(url);
		return emailModel;
	}

	@Transactional
	@Override
	public void isVerifiedUser(String token) {
		
		try {
			String storedToken = redisUtility.getSaveToken(Token.getParseJWT(token));
			System.out.println("storedToken in redis : "+storedToken); 
			redisUtility.expireSaveToken(Token.getParseJWT(token));
			if(storedToken==null)
			{
				throw new TokenExpireException("Token is Expired Please Again do Registration");
			 
			}
				
     		if (storedToken.equals(token)) {

				User user = getUserById(Long.parseLong(Token.getParseJWT(token)));

				if (user != null) {
					user.setActivated(true);
					updateUser(user);

				} else {
					throw new UserNotFoundException("User Not Found...!");
				}
			}
		} catch (SignatureException e) {
			e.printStackTrace();
		}

	}

	@Transactional
	@Override
	public String forgotPassword(String email, HttpServletRequest request) {

		User user = getUserDetailsByEmail(email);
		if (user != null) {
			String token = Token.generateToken(user.getId());

			redisUtility.saveToken(Long.toString(user.getId()), token);

			EmailModel emailModel = getEmailModel(token, request, user);

			messageSender.sendMessage(emailModel);

			return token;
		} else {

			throw new UserNotFoundException("User Not Found...!");
		}
	}

	@Transactional
	@Override
	public void restPassword(String token, String newPassword) {
		try {
			String storedToken = redisUtility.getSaveToken(Token.getParseJWT(token));
			redisUtility.expireSaveToken(Token.getParseJWT(token));
			
			if(storedToken==null)
			 throw new TokenExpireException("Token is Expired Please Again do ForgotPassword");
			
			if (storedToken.equals(token)) {
				User user = getUserById(Long.parseLong(Token.getParseJWT(token)));

				if (user != null) {
					user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));

					updateUser(user);

				} else {
					throw new UserNotFoundException("User Not Found...!");
				}
			}
		} catch (SignatureException e) {
			e.printStackTrace();
		}

	}

    @Transactional
	@Override
	public String verifyLogin(String email,String password) 
	{
	 String token="";	
	 User user = userDao.getUserDetailsByEmail(email);

	 if (user != null) {
	 if(!user.getEmail().equals(email))
	 {
		throw new UserNotFoundException("User Not Found ...!");
     }
	  
			if(BCrypt.checkpw(password, user.getPassword()) && user.isActivated())
			{
			  return token=Token.generateTokenByUserInfo(user.getId(),user.getEmail(),user.getFirstName(),user.getLastName());
			} 
			else 
			{
				System.out.println("A User with Email-Id " + email+ " and Password "
						+ password + " is Invalid");
			}
		}
	 else
	 {
		 throw new UserNotFoundException("User Not Found ...!");	 
	 }
	
		return token;
	}

    @Transactional
	@Override
	public User getCurrentUser(String token) {
		
		User user=null;
		try 
		{
			long id=Long.parseLong(Token.getParseJWT(token));
		user=userDao.getUserById(id);
			
			if(user==null)
			{
				throw new UserNotFoundException("User Not Found ...!");	
			}
			
		 	
		}
		catch (SignatureException e) 
		{
		 e.printStackTrace();
		}
		return user;
		
		
		
	}

	@Transactional
	@Override
	public void update(User user, String token) {

		System.out.println("user id : " + user.getId());

		try {

			long id = Long.parseLong(Token.getParseJWT(token));

			if (id == user.getId()) {
				User user2= userDao.getUserById(id);
				user2.setProfilePic(user.getProfilePic());
				userDao.updateUser(user2);
			} else {
				throw new UnauthorizedException("This User is Not Allow to Update Note...!");
			}
		} catch (NumberFormatException | SignatureException e) {
			e.printStackTrace();
		}

	}


	@Transactional
	@Override
	public List<?> getAllUsers(String token) 
	{
	  List<Object[]> listofUsers=null; 	
	 try 
	 {
		long id=Long.parseLong(Token.getParseJWT(token));
		User user=userDao.getUserById(id);
		
		System.out.println("Owner : "+user.getEmail());
		
		listofUsers= userDao.getAllUsers();
			
       for(int i=0;i<listofUsers.size();i++)
       {
    	System.out.println(listofUsers.get(i));
		if(user.getId() == (Long)listofUsers.get(i)[1]) 
			{
			 listofUsers.remove(listofUsers.get(i));
			 break;
			}
		}
      
	 }
	 catch (NumberFormatException | SignatureException e) 
	 {
		e.printStackTrace();
	 }	
		
		return listofUsers;
	}
}
