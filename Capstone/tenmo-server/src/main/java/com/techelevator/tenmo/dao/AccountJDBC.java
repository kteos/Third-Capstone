package com.techelevator.tenmo.dao;

import java.math.BigDecimal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.techelevator.tenmo.model.UserAccount;

@Component
public class AccountJDBC implements AccountDAO{

	private JdbcTemplate jdbcTemplate;
	
	public AccountJDBC(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Override
	public BigDecimal viewAccountBalance(int userId) {
		String sqlSelectStatement = "SELECT account_id, user_id, CAST (balance AS decimal) FROM accounts WHERE user_id = ?";
		SqlRowSet rows = jdbcTemplate.queryForRowSet(sqlSelectStatement, userId);
		UserAccount userAccount = null;
		
		while (rows.next()) {
			userAccount = mapRowToUserAccount(rows);
		}
		return userAccount.getAccountBalance();
	}
	
	private UserAccount mapRowToUserAccount(SqlRowSet rows) {
		UserAccount userAccount = new UserAccount();
		
		userAccount.setAccountBalance(rows.getBigDecimal("balance"));
		userAccount.setAccountId(rows.getInt("account_id"));
		userAccount.setUserId(rows.getInt("user_id"));
		
		return userAccount;
	}
	
	

}
