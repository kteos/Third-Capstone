package com.techelevator.tenmo.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.techelevator.tenmo.dao.AccountDAO;
import com.techelevator.tenmo.dao.UserDAO;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserAccount;
@PreAuthorize("isAuthenticated()")
@RestController
public class AccountController {

	@Autowired
	private AccountDAO accountDAO;
	
	@Autowired
	private UserDAO userDao;
	// Server is connected and accessing the database.
	// Next step is to hook the client side up and process a user input
	// passing that user input to our controller
	//
	@RequestMapping(path="/accounts/{id}", method=RequestMethod.POST)
	public UserAccount viewAccountBalance(@PathVariable(name = "id") int userId) {
		return accountDAO.viewAccountBalance(userId);
	}
//	@PreAuthorize("permitAll()")
	@RequestMapping(path = "/users" , method=RequestMethod.POST)
	public List<User> viewAllUsers(){
		return userDao.findAll();
	}
	
	
	
}
