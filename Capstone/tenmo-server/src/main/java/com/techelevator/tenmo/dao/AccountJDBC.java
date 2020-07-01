package com.techelevator.tenmo.dao;

import java.math.BigDecimal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.UserAccount;

@Component
public class AccountJDBC implements AccountDAO{

	private JdbcTemplate jdbcTemplate;
	
	public AccountJDBC(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Override
	public UserAccount viewAccountBalance(int userId) {
		String sqlSelectStatement = "SELECT account_id, user_id, CAST (balance AS decimal) FROM accounts WHERE user_id = ?";
		SqlRowSet rows = jdbcTemplate.queryForRowSet(sqlSelectStatement, userId);
		UserAccount userAccount = null;
		
		while (rows.next()) {
			userAccount = mapRowToUserAccount(rows);
		}
		return userAccount;
	}
	
	private UserAccount mapRowToUserAccount(SqlRowSet rows) {
		UserAccount userAccount = new UserAccount();
		
		userAccount.setAccountBalance(rows.getBigDecimal("balance"));
		userAccount.setAccountId(rows.getInt("account_id"));
		userAccount.setUserId(rows.getInt("user_id"));
		
		return userAccount;
	}

	@Override
	public void completeTransfer(Transfer transfer) {
		if(transfer.getTransferType() == 1) {
			String increaseSql = "UPDATE accounts SET balance = (SELECT balance)+ ? WHERE account_id = ?";
			jdbcTemplate.update(increaseSql, transfer.getAmount() , transfer.getUserId());
			String decreaseSql = "UPDATE accounts SET balance = (SELECT balance)- ? WHERE account_id = ?";
			jdbcTemplate.update(decreaseSql, transfer.getAmount() , transfer.getRecipientId());
			String transferSql = "INSERT INTO transfers (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (DEFAULT, 1, 1, ?, ?, ?)";
			jdbcTemplate.update(transferSql , transfer.getRecipientId(), transfer.getUserId()  , BigDecimal.valueOf(transfer.getAmount()));
			
		}
		else {
			String increaseSql = "UPDATE accounts SET balance = (SELECT balance)+ ? WHERE account_id = ?";
			jdbcTemplate.update(increaseSql, transfer.getAmount() , transfer.getRecipientId());
			String decreaseSql = "UPDATE accounts SET balance = (SELECT balance)- ? WHERE account_id = ?";
			jdbcTemplate.update(decreaseSql, transfer.getAmount() , transfer.getUserId());
			String transferSql = "INSERT INTO transfers (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (DEFAULT, 2, 2, ?, ?, ?)";
			jdbcTemplate.update(transferSql , transfer.getUserId(), transfer.getRecipientId() , BigDecimal.valueOf(transfer.getAmount()));
		}
		
	}
	
	

}
