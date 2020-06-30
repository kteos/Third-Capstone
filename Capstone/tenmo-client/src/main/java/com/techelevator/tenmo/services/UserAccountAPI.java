package com.techelevator.tenmo.services;

import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.models.UserAccount;
import com.techelevator.tenmo.models.UserAccountDAO;

public class UserAccountAPI implements UserAccountDAO {
	
	private String baseUrl; 
	private RestTemplate restTemplate;
	
public  UserAccountAPI( String baseUrl) {
	this.baseUrl = baseUrl;
	restTemplate = new RestTemplate();
}

	@Override
	public UserAccount viewAccountBalance(int userId) {
		UserAccount newUserAccount = restTemplate.getForObject(baseUrl + "accounts/" + userId, UserAccount.class);
		return newUserAccount;
	}
}
