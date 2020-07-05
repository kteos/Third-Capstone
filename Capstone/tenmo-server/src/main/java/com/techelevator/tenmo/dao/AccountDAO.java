package com.techelevator.tenmo.dao;

import java.util.List;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.UserAccount;

public interface AccountDAO {

	UserAccount viewAccountBalance(int userId );
	void completeTransfer(Transfer transfer);
	List<Transfer>getAllTransfers(int userId);
	List<Transfer>getPendingTransfers(int userId);
	void transferApprovalOrDenial(Transfer transfer, int approveOrRejectStatus);
	
}
