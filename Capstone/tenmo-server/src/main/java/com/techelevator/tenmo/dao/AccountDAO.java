package com.techelevator.tenmo.dao;

import java.math.BigDecimal;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.UserAccount;

public interface AccountDAO {

	UserAccount viewAccountBalance(int userId );
	void completeTransfer(Transfer transfer);
	
	
}
