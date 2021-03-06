package com.bridgelabz.todo.userservice.dao;

 import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.bridgelabz.todo.userservice.model.User;

@Repository
public class UserDaoImpl implements IUserDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public void insert(User user) {

		Session session = sessionFactory.getCurrentSession();
		session.save(user);
		System.out.println("Registration is successfully done...!");
	}

	@Override
	public User getUserDetailsByEmail(String email) {
		Session session = sessionFactory.getCurrentSession();
		@SuppressWarnings("deprecation")
		Criteria criteria = session.createCriteria(User.class).add(Restrictions.eq("email", email));
		User user = (User) criteria.uniqueResult();
		return user;

	}

	@Override
	public long isUserExist(String email) {
		Session session = sessionFactory.getCurrentSession();
		@SuppressWarnings("deprecation")
		Criteria criteria = session.createCriteria(User.class).add(Restrictions.eq("email", email))
				.setProjection(Projections.count("email"));

		long count = (long) criteria.uniqueResult();

		return count;

	}

	@Override
	public User getUserById(long id) {
		Session session = sessionFactory.getCurrentSession();
		@SuppressWarnings("deprecation")
		Criteria criteria = session.createCriteria(User.class).add(Restrictions.eq("id", id));

		User user = (User) criteria.uniqueResult();
		return user;
	}

	@Override
	public void updateUser(User user) {
		Session session = sessionFactory.getCurrentSession();
		session.update(user);
		System.out.println("User successfully Updated...!");
	}

		@Override
		public List<Object[]> getAllUsers()
		{
			Session session=sessionFactory.getCurrentSession();
			Query q = session.createQuery("select email,id from User ");
			List<Object[]> list = q.getResultList();
			return list;
			
			
		}

}