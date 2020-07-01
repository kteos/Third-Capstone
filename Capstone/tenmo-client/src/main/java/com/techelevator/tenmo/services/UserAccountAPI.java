package com.techelevator.tenmo.services;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import javax.jws.soap.SOAPBinding.Use;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;
import com.techelevator.tenmo.models.UserAccount;
import com.techelevator.tenmo.models.UserAccountDAO;
import com.techelevator.tenmo.models.UserCredentials;

public class UserAccountAPI implements UserAccountDAO {
	
	private String baseUrl; 
	private RestTemplate restTemplate;
	
	
public  UserAccountAPI( String baseUrl ) {
	this.baseUrl = baseUrl;
	restTemplate = new RestTemplate();
	
}

	@Override
	public UserAccount viewAccountBalance(int userId , String token) {
		HttpEntity entity = createRequestEntity(token);
		
		UserAccount newUserAccount = restTemplate.exchange(baseUrl + "accounts/" + userId , HttpMethod.POST , entity , UserAccount.class).getBody();
//		UserAccount newUserAccount = restTemplate.getForObject(baseUrl + "accounts/" + userId, UserAccount.class);
		return newUserAccount;
	}
	
	
	@Override
	public List<User> viewAll(String token) {
		HttpEntity entity = createRequestEntity(token);
		
		User[] arrayOfUsers = restTemplate.exchange(baseUrl+"users",  HttpMethod.POST, entity, User[].class).getBody();
		
		List<User> allUsers = Arrays.asList(arrayOfUsers);
		return allUsers;
	}
	
	
	
	
	
	private HttpEntity createRequestEntity(String token) {
    	HttpHeaders headers = new HttpHeaders();
    	headers.setBearerAuth(token);
    	HttpEntity entity = new HttpEntity<>(headers);
    	return entity;
    }

	@Override
	public void createTransfer(Transfer transfer, String token) {
//		HttpEntity entity = createRequestEntity(token);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token);
		HttpEntity<Transfer> transferRequest = new HttpEntity<Transfer>(transfer, headers);
		 
		restTemplate.postForObject(baseUrl+ "transfer", transferRequest, Transfer.class);
		
//		restTemplate.exchange(baseUrl+ "transfer", HttpMethod.POST , transferRequest, Transfer.class);

		
	}

	@Override
	public List<Transfer> listOfUserTransfers(int userId, String token) {
		HttpEntity entity = createRequestEntity(token);
		Transfer[] arrayOfTransfers = restTemplate.exchange(baseUrl + "transfer/" + userId , HttpMethod.POST , entity, Transfer[].class).getBody();
		List<Transfer> listOfTransfers = Arrays.asList(arrayOfTransfers);

		return listOfTransfers;
	}

	
}
