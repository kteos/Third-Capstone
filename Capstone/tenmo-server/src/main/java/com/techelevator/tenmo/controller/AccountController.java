package com.techelevator.tenmo.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.techelevator.tenmo.dao.AccountDAO;
import com.techelevator.tenmo.dao.UserDAO;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserAccount;
@PreAuthorize("isAuthenticated()")
@RestController
public class AccountController {

	@Autowired
	private AccountDAO accountDAO;
	
	@Autowired
	private UserDAO userDao;
	
	@RequestMapping(path="/accounts/{id}", method=RequestMethod.POST)
	public UserAccount viewAccountBalance(@PathVariable(name = "id") int userId) {
		return accountDAO.viewAccountBalance(userId);
	}

	@RequestMapping(path = "/users" , method=RequestMethod.POST)
	public List<User> viewAllUsers(){
		return userDao.findAll();
	}
	
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(path = "/transfer" , method=RequestMethod.POST)
	public void makeATrasnfer(@RequestBody Transfer transfer) {
		accountDAO.completeTransfer(transfer);
	}
	
	@RequestMapping(path="/transfer/{id}", method=RequestMethod.POST)
	public List<Transfer>viewAllTransfersById(@PathVariable(name="id") int userId){
		return accountDAO.getAllTransfers(userId);
	}
	
	@RequestMapping(path= "/transfer/pending/{id}" , method=RequestMethod.POST)
	public List<Transfer> viewAllPendingTransferById(@PathVariable(name="id") int userId){
		return accountDAO.getPendingTransfers(userId);
	}
	
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(path= "/transfer/statusupdate/{id}", method=RequestMethod.POST)
	public void acceptOrRejectTransfer(@PathVariable(name="id") int approveOrRejectStatus, @RequestBody Transfer transfer) {
		accountDAO.transferApprovalOrDenial(transfer, approveOrRejectStatus);
	}
	
		
	
	
	
	
}
