package com.techelevator.tenmo.models;

import java.math.BigDecimal;
import java.util.List;

public interface UserAccountDAO {

	
	UserAccount viewAccountBalance(int userId , String token);
	List<User> viewAll(String token);
}
