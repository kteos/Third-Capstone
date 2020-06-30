package com.techelevator.tenmo.dao;

import java.math.BigDecimal;

import com.techelevator.tenmo.model.UserAccount;

public interface AccountDAO {

	UserAccount viewAccountBalance(int userId);
	
}
