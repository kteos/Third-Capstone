package com.techelevator.tenmo.controller;


import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.techelevator.tenmo.dao.AccountDAO;

@RestController
public class AccountController {

	@Autowired
	private AccountDAO accountDAO;
	// Server is connected and accessing the database.
	// Next step is to hook the client side up and process a user input
	// passing that user input to our controller
	//
	@RequestMapping(path="/accounts/{id}", method=RequestMethod.GET)
	public BigDecimal viewAccountBalance(@PathVariable(name = "id") int userId) {
		return accountDAO.viewAccountBalance(userId);
	}
	
	
	
}
