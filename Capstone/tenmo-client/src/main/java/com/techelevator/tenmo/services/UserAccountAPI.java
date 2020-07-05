package com.techelevator.tenmo.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;
import com.techelevator.tenmo.models.UserAccount;
import com.techelevator.tenmo.models.UserAccountDAO;

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
		UserAccount newUserAccount = null;
		try {
			newUserAccount = restTemplate.exchange(baseUrl + "accounts/" + userId , HttpMethod.POST , entity , UserAccount.class).getBody();
		} catch (RestClientResponseException ex) {
			System.out.println(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return newUserAccount;
	}
	
	@Override
	public List<User> viewAll(String token) {
		HttpEntity entity = createRequestEntity(token);
		User[] arrayOfUsers = null;
		try {
			arrayOfUsers = restTemplate.exchange(baseUrl+"users",  HttpMethod.POST, entity, User[].class).getBody();
		} catch (RestClientResponseException ex) {
			System.out.println(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		List<User> allUsers = Arrays.asList(arrayOfUsers);
		return allUsers;
	}
	
	@Override
	public void createTransfer(Transfer transfer, String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token);
		HttpEntity<Transfer> transferRequest = new HttpEntity<Transfer>(transfer, headers);
		 
		try {
			restTemplate.postForObject(baseUrl+ "transfer", transferRequest, Transfer.class);
		} catch (RestClientResponseException ex) {
			System.out.println(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
	}

	@Override
	public List<Transfer> listOfUserTransfers(int userId, String token) {
		HttpEntity entity = createRequestEntity(token);
		Transfer[] arrayOfTransfers = null;
		try {
			arrayOfTransfers = restTemplate.exchange(baseUrl + "transfer/" + userId , HttpMethod.POST , entity, Transfer[].class).getBody();
		} catch (RestClientResponseException ex) {
			System.out.println(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		List<Transfer> listOfTransfers = Arrays.asList(arrayOfTransfers);
		return listOfTransfers;
	}

	private HttpEntity createRequestEntity(String token) {
    	HttpHeaders headers = new HttpHeaders();
    	headers.setBearerAuth(token);
    	HttpEntity entity = new HttpEntity<>(headers);
    	return entity;
    }

	@Override
	public List<Transfer> getPendingTransfers(int userId, String token) {
		HttpEntity entity = createRequestEntity(token);
		Transfer[] arrayOfTransfers = null;
		try {
			arrayOfTransfers = restTemplate.exchange(baseUrl + "transfer/pending/" + userId , HttpMethod.POST , entity, Transfer[].class).getBody();
		} catch (RestClientResponseException ex) {
			System.out.println(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		List<Transfer> listOfTransfers = Arrays.asList(arrayOfTransfers);
		return listOfTransfers;
	}	
	
	@Override
	public void acceptOrRejectTransfer(Transfer transfer, int approveOrRejectStatus, String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token);
		HttpEntity<Transfer> transferRequest = new HttpEntity<Transfer>(transfer, headers);
		 
		try {
			restTemplate.postForObject(baseUrl+ "transfer/" + "statusupdate/" + approveOrRejectStatus, transferRequest, Transfer.class);
		} catch (RestClientResponseException ex) {
			System.out.println(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
	}
	
}
