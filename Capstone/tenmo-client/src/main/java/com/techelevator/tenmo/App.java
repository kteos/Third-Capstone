package com.techelevator.tenmo;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.sound.midi.Receiver;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;
import com.techelevator.tenmo.models.UserAccount;
import com.techelevator.tenmo.models.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.tenmo.services.UserAccountAPI;
import com.techelevator.view.ConsoleService;

public class App {

private static final String API_BASE_URL = "http://localhost:8080/";
    
    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	
    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private UserAccountAPI userAccountAPI;
    
    

    public static void main(String[] args) {
    	App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
    	app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService) {
		this.console = console;
		this.authenticationService = authenticationService;
		
		this.userAccountAPI = new UserAccountAPI(API_BASE_URL);
		
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");
		
		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
		while(true) {
			String choice = (String)console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if(MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if(MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if(MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if(MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if(MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if(MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	private void viewCurrentBalance() {
		
		UserAccount user = userAccountAPI.viewAccountBalance(currentUser.getUser().getId() , currentUser.getToken());
		
		System.out.println("Your current balance $" + user.getAccountBalance());
		
	}

	private void viewTransferHistory() {
		List<Transfer> listOfTransfers = userAccountAPI.listOfUserTransfers(currentUser.getUser().getId(), currentUser.getToken());
		for(Transfer a : listOfTransfers) {
			System.out.print(a.getTransferId() + "    ");
				if(a.getTransferType() == 2) {
					System.out.print("To: " + a.getRecipientName() + "    ");
				} else {
					System.out.print("From: " + a.getSenderName() + "    ");
				}
			System.out.print(a.getAmount());
			System.out.println();
		}
		int userSelectedTransferId = console.getUserInputInteger("Please enter transfer ID to view details >>>");
		//TODO adjust transfer object to contain from and to names 
		// adjust sql command to add join
		for( Transfer b:listOfTransfers ) {
			if(b.getTransferId() == userSelectedTransferId) {
				
					System.out.println("ID: " + b.getTransferId());
					System.out.println("From: " + b.getSenderName());
					System.out.println("To: " + b.getRecipientName());
					// TODO Method to change transfer type from 1-3 to Send, Request etc
					System.out.println("Type: " + b.getTransferType());
					System.out.println("Status: " + b.getTransferStatusId());
					System.out.println("Amount: " + b.getAmount());
				System.out.println();
			}
		}
	}

	private void viewPendingRequests() {
		List<Transfer> pendingTranfers = userAccountAPI.getPendingTransfers(currentUser.getUser().getId(), currentUser.getToken());
		pendingTransferPrettyPrinter(pendingTranfers);
	}

	private void sendBucks() {
		List<User> list = userAccountAPI.viewAll(currentUser.getToken());
		usersPrettyPrinter(list, currentUser.getUser().getId());
		int userId = userIdSelector(list, currentUser.getUser().getId());
		int amountToTransfer = console.getUserInputInteger("Enter amount >>" );
		UserAccount currentUserAccount = userAccountAPI.viewAccountBalance(currentUser.getUser().getId(), currentUser.getToken());
//		if (BigDecimal.valueOf(amountToTransfer) > currentUserAccount.getAccountBalance()) {
//			System.out.println("Sorry, you don't have the available funds");
//			return;
//		}
//		
//		if (currentUserAccount.getAccountBalance().compareTo(val))
		Transfer transfer = transferBuilder(amountToTransfer, 2, userId, currentUser.getUser().getId());
		userAccountAPI.createTransfer(transfer, currentUser.getToken());
		System.out.println("Transfer successfully sent!");
		
	}

	private void requestBucks() {
		List<User> list = userAccountAPI.viewAll(currentUser.getToken());
		usersPrettyPrinter(list, currentUser.getUser().getId());
		int userId = userIdSelector(list, currentUser.getUser().getId());
		int amountToTransfer = console.getUserInputInteger("Enter amount >>" );
		Transfer transfer = transferBuilder(amountToTransfer, 1, userId, currentUser.getUser().getId());
		userAccountAPI.createTransfer(transfer, currentUser.getToken());
		System.out.println("Request successfully made!");
	}
	
	private void exitProgram() {
		System.exit(0);
	}

	private void registerAndLogin() {
		while(!isAuthenticated()) {
			String choice = (String)console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
            	authenticationService.register(credentials);
            	isRegistered = true;
            	System.out.println("Registration successful. You can now login.");
            } catch(AuthenticationServiceException e) {
            	System.out.println("REGISTRATION ERROR: "+e.getMessage());
				System.out.println("Please attempt to register again.");
            }
        }
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) //will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
		    try {
				currentUser = authenticationService.login(credentials);
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: "+e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}
	
	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}
	
	private void usersPrettyPrinter(List<User> users, int currentUserId) {
		System.out.println(String.join("", Collections.nCopies(20, "--")));
		System.out.println("Users");
		System.out.printf("%-10s", "ID");
		System.out.printf("%-10s", "Name");
		System.out.println();
		System.out.println(String.join("", Collections.nCopies(20, "--")));
		for(User a : users) {
			if (a.getId() != currentUserId) {
				System.out.printf("%-10s", a.getId());
				System.out.printf("%-10s", a.getUsername());
				System.out.println();
			}
		}
		System.out.println(String.join("", Collections.nCopies(20, "--")));
	}
	
	private boolean userIdValidTest(List<User> users, int recipientId, int currentUserId) {
		boolean recipientChecker = false;
		for (User a : users) {
			if ((a.getId() == recipientId) && (a.getId() != currentUserId)) {
				recipientChecker = true;
			}
		}
		if (!recipientChecker) {
			System.out.println("Please select a valid User ID");
			return false;
		}
		return true;
	}
	
	private void pendingTransferPrettyPrinter(List<Transfer> pendingTranfers) {
		System.out.println("Pending Tranfers");
		System.out.printf("%-10s", "ID");
		System.out.printf("%-10s", "Name");
		System.out.printf("%-10s", "Amount");
		System.out.println();
		System.out.println(String.join("", Collections.nCopies(20, "--")));
		for( Transfer a : pendingTranfers) {
			if (a.getTransferType() == 1) {
				System.out.printf("%-10s", a.getTransferId());
				System.out.printf("%-10s", a.getSenderName());
				System.out.printf("%-10s", "$" + a.getAmount());
				System.out.println();
			}
		}
		System.out.println(String.join("", Collections.nCopies(20, "--")));
	}
	
	private Transfer transferBuilder(int amountToTransfer, int transferType, int userId, int currentUserId) {
		Transfer transfer = new Transfer();
		transfer.setAmount(BigDecimal.valueOf(amountToTransfer));
		transfer.setRecipientId(userId);
		transfer.setTransferType(transferType);
		transfer.setUserId(currentUserId);
		return transfer;
	}
	
	private int userIdSelector(List<User> users, int currentUserId) {
		boolean userChecker = false;
		int userId = 0;
		while (!userChecker) {
			userId = console.getUserInputInteger("Select a user ID>>>");
			userChecker = userIdValidTest(users, userId, currentUserId);
		}
		return userId;
	}
}
