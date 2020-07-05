package com.techelevator.tenmo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	private Map<Integer, String>transferStatus = new HashMap<Integer, String>() {{
		put(1, "Pending");
		put(2, "Approved");
		put(3, "Rejected");
	}};
	
	private Map<Integer, String>transferTypes = new HashMap<Integer, String>() {{
		put(1, "Request");
		put(2, "Send");
	}};
	
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
		historyTransferPrettyPrinter(listOfTransfers);
		int userSelectedTransferId = transferIdSelectorWithFundsCheck();
		if (userSelectedTransferId == 0) {
			return;
		}
		transferDetailsPrettyPrinter(userSelectedTransferId, listOfTransfers);
	}

	private void viewPendingRequests() {
		List<Transfer> pendingTransfers = userAccountAPI.getPendingTransfers(currentUser.getUser().getId(), currentUser.getToken());
		int pendingTransferCounter = pendingTransferPrettyPrinter(pendingTransfers);
		if (pendingTransferCounter == 0) {
			return;
		}
		
		int userSelectedTransferId = pendingRequestIdSelector(pendingTransfers);
		if (userSelectedTransferId == 0) {
			return;
		}
		
		Transfer userSelectedTransfer = transferFinder(pendingTransfers, userSelectedTransferId);
		double transferAmount = userSelectedTransfer.getAmount().doubleValue();
		
		int userApproveOrRejectSelection = approveOrRejectPrinter();
		
		if (userApproveOrRejectSelection == 0) {
			return;
		} else if (userApproveOrRejectSelection == 1) {
			if (userSelectedTransfer.getRecipientId() == currentUser.getUser().getId()) {
				System.out.println("You cannot accept a request that you sent.");
				return;
			}
			approvalHandler(transferAmount, userSelectedTransfer, userApproveOrRejectSelection);
		} else if (userApproveOrRejectSelection == 2){
			rejectionHandler(userSelectedTransfer, userApproveOrRejectSelection);
		}
	}


	private void sendBucks() {
		List<User> list = userAccountAPI.viewAll(currentUser.getToken());
		usersPrettyPrinter(list, currentUser.getUser().getId());
		int userId = userIdSelector(list, currentUser.getUser().getId());
		if (userId == 0) {
			return;
		}
		double amountToTransfer = transferAmountSelector();
		if (amountToTransfer == 0) {
			System.out.println("Sorry, your account has a $0 balance");
			return;
		}
		Transfer transfer = transferBuilder(amountToTransfer, 2, userId, currentUser.getUser().getId());
		userAccountAPI.createTransfer(transfer, currentUser.getToken());
		System.out.println("Transfer successfully sent!");
		
	}

	private void requestBucks() {
		List<User> list = userAccountAPI.viewAll(currentUser.getToken());
		usersPrettyPrinter(list, currentUser.getUser().getId());
		int userId = userIdSelector(list, currentUser.getUser().getId());
		if (userId == 0) {
			return;
		}
		double amountToTransfer = console.getUserInputDouble("Enter amount >>" );
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
	
	private int userIdSelector(List<User> users, int currentUserId) {
		boolean userChecker = false;
		int userId = 0;
		while (!userChecker) {
			userId = console.getUserInputInteger("Enter ID of user you are sending to (0 to cancel): ");
			if (userId == 0) {
				return userId;
			}
			userChecker = userIdValidTest(users, userId, currentUserId);
		}
		return userId;
	}
	
	private Transfer transferBuilder(double amountToTransfer, int transferType, int userId, int currentUserId) {
		Transfer transfer = new Transfer();
		transfer.setAmount(BigDecimal.valueOf(amountToTransfer));
		transfer.setRecipientId(userId);
		transfer.setTransferType(transferType);
		transfer.setUserId(currentUserId);
		return transfer;
	}
	
	private double transferAmountSelector() {
		boolean amountChecker = false;
		double transferAmount = 0;
		if (fundsGreaterThanZeroChecker()) {
			while (!amountChecker) {
				transferAmount = console.getUserInputDouble("Enter amount >>" );
				amountChecker = availableFundsChecker(transferAmount);
			}
		}
		return transferAmount;
	}
	
	private int transferIdSelectorWithFundsCheck() {
		boolean transferChecker = false;
		int transferId = 0;
			while (!transferChecker) {
				transferId = console.getUserInputInteger("Please enter transfer ID to view details (0 to cancel): ");
				if (transferId == 0) {
					return transferId;
				}
				transferChecker = availableFundsChecker(transferId);
			}
		return transferId;
	}
	
	private Transfer transferFinder(List<Transfer> pendingTransfers, int userSelectedTransferId) {
		Transfer nonreturningtransfer = null;
		for(Transfer transfer : pendingTransfers) {
			if (transfer.getTransferId() == userSelectedTransferId) {
				return transfer;
			}
		}
		return nonreturningtransfer;
	}
	
	private int pendingRequestIdSelector(List<Transfer> pendingTranfers) {
		boolean pendingChecker = false;
		int transferId = 0;
			while (!pendingChecker) {
				transferId = console.getUserInputInteger("Please enter transfer ID to approve/reject (0 to cancel)");
				if (transferId == 0) {
					return transferId;
				}
				pendingChecker = validTransferIdRequest(pendingTranfers, transferId);
			}
		return transferId;
	}
	
	private boolean validTransferIdRequest(List<Transfer> pendingTranfers, int transferId) {
		for(Transfer transfer : pendingTranfers) {
			if (transfer.getTransferId() == transferId) {
				return true;
			}
		} return false;
	}
	
	private void approvalHandler(double transferAmount, Transfer userSelectedTransfer, int userApproveOrRejectSelection) {
		if (availableFundsChecker(transferAmount)) {
			userAccountAPI.acceptOrRejectTransfer(userSelectedTransfer, userApproveOrRejectSelection, currentUser.getToken());
			System.out.println("Transfer Approved!");
		}
	}
	
	private void rejectionHandler(Transfer userSelectedTransfer, int userApproveOrRejectSelection) {
		userAccountAPI.acceptOrRejectTransfer(userSelectedTransfer, userApproveOrRejectSelection, currentUser.getToken());
		System.out.println("Transfer Rejected");
	}
	
	private boolean availableFundsChecker(double amountToTransfer) {
		UserAccount currentUserAccount = userAccountAPI.viewAccountBalance(currentUser.getUser().getId(), currentUser.getToken());
		if (currentUserAccount.getAccountBalance().intValue() < amountToTransfer) {
			System.out.println("Sorry, you don't have the available funds. Please try again.");
			return false;
		}
		if (amountToTransfer < 0) {
			System.out.println("You have to have at least $1 in your account. Please try again.");
			return false;
		}
		return true;
	}
	
	private boolean fundsGreaterThanZeroChecker() {
		UserAccount currentUserAccount = userAccountAPI.viewAccountBalance(currentUser.getUser().getId(), currentUser.getToken());
		if (currentUserAccount.getAccountBalance().intValue() == 0) {
			return false;
		}
		return true;
	}
	
	private void historyTransferPrettyPrinter(List<Transfer> pastTranfers) {
		System.out.println(String.join("", Collections.nCopies(20, "--")));
		System.out.println("Transfers");
		System.out.printf("%-10s", "ID");
		System.out.printf("%-15s", "From/To");
		System.out.printf("%-10s", "Amount");
		System.out.println();
		System.out.println(String.join("", Collections.nCopies(20, "--")));
		for(Transfer a : pastTranfers) {
			if ((a.getTransferStatusId() == 2) || (a.getTransferStatusId() == 3)) {
			System.out.printf("%-10s", a.getTransferId());
				if(a.getTransferType() == 2) {
					System.out.printf("%-15s", "To: " + a.getRecipientName());
				} else {
					System.out.printf("%-15s", "From: " + a.getSenderName());
				}
			System.out.printf("%-10s", "$" + a.getAmount());
			System.out.println();
			}
		}
	}
	
	private void transferDetailsPrettyPrinter(int userSelectedTransferId, List<Transfer> listOfTransfers) {
		System.out.println(String.join("", Collections.nCopies(20, "--")));
		System.out.println("Transfer Details");
		System.out.println(String.join("", Collections.nCopies(20, "--")));
		for( Transfer b : listOfTransfers ) {
			if(b.getTransferId() == userSelectedTransferId) {
					System.out.println("ID: " + b.getTransferId());
					System.out.println("From: " + b.getSenderName());
					System.out.println("To: " + b.getRecipientName());
					System.out.println("Type: " + transferTypes.get(b.getTransferType()));
					System.out.println("Status: " + transferStatus.get(b.getTransferStatusId()));
					System.out.println("Amount: $" + b.getAmount());
			}
		}
	}
	
	private int pendingTransferPrettyPrinter(List<Transfer> pendingTranfers) {
		int transferCounter = 0;
		System.out.println("Pending Tranfers");
		System.out.printf("%-10s", "ID");
		System.out.printf("%-10s", "To");
		System.out.printf("%-10s", "Amount");
		System.out.println();
		System.out.println(String.join("", Collections.nCopies(20, "--")));
		for( Transfer a : pendingTranfers) {
			if (a.getTransferStatusId() == 1) {
				System.out.printf("%-10s", a.getTransferId());
				System.out.printf("%-10s", a.getSenderName());
				System.out.printf("%-10s", "$" + a.getAmount());
				System.out.println();
				transferCounter += 1;
			}
		}
		System.out.println(String.join("", Collections.nCopies(20, "--")));
		if (transferCounter == 0) {
			System.out.println("You have no pending transfers.");
		}
		return transferCounter;
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
	
	private int approveOrRejectPrinter() {
		boolean userSelectionChecker = false;
		int userSelection = 0;
		while (!userSelectionChecker) {
			System.out.println("1: Approve");
			System.out.println("2: Reject");
			System.out.println("0: Don't Approve or Reject");
			System.out.println(String.join("", Collections.nCopies(4, "--")));
			userSelection = console.getUserInputInteger("Please choose an option: ");
			if (approveOrRejectChecker(userSelection)) {
				userSelectionChecker = true;
				return userSelection;
			}
			System.out.println("Sorry, please choose a valid option");
		}
		return userSelection;
	}
	
	private boolean approveOrRejectChecker(int userSelection) {
		ArrayList<Integer> possibleSelections = new ArrayList<Integer>();
		possibleSelections.add(1);
		possibleSelections.add(2);
		possibleSelections.add(0);
		
		if (possibleSelections.contains(userSelection)) {
			return true;
		}
		return false;
	}
	
}
