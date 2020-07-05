package com.techelevator.tenmo.models;

import java.util.List;

public interface UserAccountDAO {

	
	UserAccount viewAccountBalance(int userId , String token);
	List<User> viewAll(String token);
	void createTransfer(Transfer transfer, String token);
	List<Transfer> listOfUserTransfers(int userId, String token);
	List<Transfer> getPendingTransfers(int userId , String token);
	void acceptOrRejectTransfer(Transfer transfer, int approveOrRejectStatus, String token);
}
