//Julie De Lorenzo
//Don Virostek

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class team04{
	private Connection connection;		//used to hold the jdbc connection to the DB
	private Statement statement;		//used to create an instance of the connection
	private ResultSet resultSet;		//used to hold the result of your query (if one exists)
	private String query;				//this will hold the query we are using
	private PreparedStatement preparedStatement;  //holds prepared statements
	private String username, password;

	public team04(){
		
		//connect to the DB
		username = "djv7";
		password = "3501723";
		//username = "jed76";
		//password = "3503235";
		try{
			//Register the oracle driver.  This needs the oracle files provided
			//in the oracle.zip file, unzipped into the local directory and 
			//the class path set to include the local directory
			DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
			
			//This is the location of the database.  This is the database in oracle
			//provided to the class
			String url = "jdbc:oracle:thin:@db10.cs.pitt.edu:1521:dbclass"; 
      
			//create a connection to DB on db10.cs.pitt.edu
			connection = DriverManager.getConnection(url, username, password); 
		}
		catch(Exception Ex){//What to do with any exceptions
			System.out.println("Error connecting to database.  Machine Error: " + Ex.toString());
			Ex.printStackTrace();
		}
		
		//main loop
		int userInput = 0;
		int currentUserId = -1;
		Date lastLogin = null;
		Scanner scanner = new Scanner(System.in);
		while(userInput != -1){
			
			//login or register mode
			if(currentUserId == -1){
				System.out.println("Choose:  Login(1), Register(2), or exit(-1)");
				try{
					userInput = scanner.nextInt();
					scanner.nextLine();
				}
				catch(Exception Ex){
					//user did not input an int, set user input to 15 (an invalid choice)
					scanner.nextLine();
					userInput = 15;
				}
				
				
				if(userInput == 1){//login task
					
					//ask for email and password
					String email = "";
					String password = "";
					System.out.println("Enter email:");
					email = scanner.nextLine();
					System.out.println("Enter password:");
					password = scanner.nextLine();
					
					//query DB to find if the user exists
					boolean userExists = false;
					int userId = -1;
					try{
						query = "SELECT userID FROM profile WHERE email=? AND password=?";
						preparedStatement = connection.prepareStatement(query);
						preparedStatement.setString(1, email);
						preparedStatement.setString(2, password);
						
			
						resultSet = preparedStatement.executeQuery();
			
						userExists = resultSet.next();
						if(userExists){
							userId = Integer.parseInt(resultSet.getString(1));
						}
					}
					catch(Exception Ex){
						System.out.println("Error trying to login.  Machine Error: " + Ex.toString());
					}
					
					if(userExists){
						//Successful login
						currentUserId = userId;
						
						//record previous lastLogin (for use in task 6)
						try{
							statement = connection.createStatement();
							query = "SELECT lastLogin FROM profile WHERE userID=?";
							preparedStatement = connection.prepareStatement(query);
							preparedStatement.setInt(1, userId);
				
							resultSet = preparedStatement.executeQuery();
				
							resultSet.next();
							lastLogin = resultSet.getDate(1);
						}
						catch(Exception Ex){
							System.out.println("Error trying to record previous lastLogin.  Machine Error: " + Ex.toString());
						}
						
						
						//update lastlogin
						try{
							statement = connection.createStatement();
							query = "Update profile set lastlogin = CURRENT_DATE WHERE userID="+userId;
				
							resultSet = statement.executeQuery(query);
						}
						catch(Exception Ex){
							System.out.println("Error trying to update last login.  Machine Error: " + Ex.toString());
						}
					}
					else{
						//failed login
						System.out.println("Invalid login information.");
					}
					
				}
				else if(userInput == 2){//register task
					
					//get all profile information from user
					String name = "";
					String email = "";
					String password = "";
					String birthday = "";
					String selfDescription = "";
					String imageUrl = "";
					System.out.println("Enter name:");
					name = scanner.nextLine();
					System.out.println("Enter email:");
					email = scanner.nextLine();
					System.out.println("Enter password:");
					password = scanner.nextLine();
					System.out.println("Enter birthday (yyyy-mm-dd):");
					birthday = scanner.nextLine();
					System.out.println("Enter self description:");
					selfDescription = scanner.nextLine();
					System.out.println("Enter image url:");
					imageUrl = scanner.nextLine();
					
					boolean userInputValid = true;
					
					//confirm date format
					try{
						Date.valueOf(birthday);
					}
					catch(IllegalArgumentException Ex){
						userInputValid = false;
						System.out.println("Invalid birthday format.");
					}
					
					//verify email
					if(!email.matches(".*@pitt.edu$")){
						userInputValid = false;
						System.out.println("Email must be @pitt.edu.");
					}
					
					if(userInputValid){
						//get max userID from profile table (the userID for this new profile will be max(userID)+1)
						int maxUserId = -1;
						try{
							statement = connection.createStatement();
							statement.executeQuery("SET TRANSACTION READ WRITE");
							
							statement = connection.createStatement();
							query = "SELECT NVL(MAX(userID), 0) FROM profile";
				
							resultSet = statement.executeQuery(query);
				
							resultSet.next();
							maxUserId = resultSet.getInt(1);
							
						}
						catch(Exception Ex){
							System.out.println("Error getting max userID.  Machine Error: " +
									Ex.toString());
						}
						
						//insert new profile into DB
						try{
							query = "INSERT INTO profile VALUES(?, ?, ?, ?, TO_DATE(?, \'yyyy-mm-dd\'), ?,  ?, CURRENT_DATE)";
							preparedStatement = connection.prepareStatement(query);
							preparedStatement.setInt(1, maxUserId+1);
							preparedStatement.setString(2, name);
							preparedStatement.setString(3, email);
							preparedStatement.setString(4, password);
							preparedStatement.setString(5, birthday);
							preparedStatement.setString(6, imageUrl);
							preparedStatement.setString(7, selfDescription);
				
							resultSet = preparedStatement.executeQuery();
							
							statement = connection.createStatement();
							statement.executeQuery("COMMIT");
							
						}
						catch(Exception Ex){
							System.out.println("Error inserting new profile.  Machine Error: " +
									Ex.toString());
						}
						
						//record previous lastLogin (for use in task 6)
						try{
							
							query = "SELECT lastLogin FROM profile WHERE userID=?";
							preparedStatement = connection.prepareStatement(query);
							preparedStatement.setInt(1, maxUserId+1);
				
							resultSet = preparedStatement.executeQuery();
				
							resultSet.next();
							lastLogin = resultSet.getDate(1);
						}
						catch(Exception Ex){
							System.out.println("Error trying to record previous lastLogin.  Machine Error: " + Ex.toString());
						}
						
						currentUserId = (maxUserId+1);
						System.out.println("You have been registered!");
					}
					
				}
				else if(userInput == -1){
					//let the loop break
				}
				else{
					//invalid input, ask again...
					System.out.println("Invalid input!");
				}
				
			}
			
			//already logged in, ask for a task
			else{
				
				System.out.println("Choose:");
				System.out.println("Send Message(3)");
				System.out.println("Add Friend(4)");
				System.out.println("Display All Message(5)");
				System.out.println("Display All New Messages(6)");
				System.out.println("Display Friends(7)");
				System.out.println("Find User(8)");
				System.out.println("Confirm Friends Requests(9)");
				System.out.println("Three Degrees of Separation(10)");
				System.out.println("Join A Group(11)");
				System.out.println("My Statistics(12)");
				System.out.println("Drop Account(13)");
				System.out.println("Log Out(14)");
				System.out.println("Exit(-1)");
				try{
					userInput = scanner.nextInt();
					scanner.nextLine();
				}
				catch(Exception Ex){
					//user did not input an int, set user input to 15 (an invalid choice)
					scanner.nextLine();
					userInput = 15;
				}
				
				if(userInput == 3){//Send Message task
					
					//ask user which type of message this is (to user or to group)
					System.out.println("Choose:  send to user(a) or send to group(b)");
					String messageTypeInput = scanner.nextLine();
					
					if(messageTypeInput.compareTo("a")==0){//send to user
						//display all friends and record valid toUserIds
						ArrayList<Integer> toUserIds = new ArrayList<Integer>();
						try{
							statement = connection.createStatement();
							query = "SELECT userID1, userID2, a.email, b.email FROM ((Friends f join Profile a on f.userID1=a.userID) join Profile b on f.userID2=b.userID) WHERE userID1="+currentUserId+" OR userID2="+currentUserId;
				
							resultSet = statement.executeQuery(query);
				
							int counter = 1;
							System.out.println("Choose a Friend:");
							while(resultSet.next()){
								if(resultSet.getInt(1)!=currentUserId){//userID1 is the friend
									System.out.println("ID: "+resultSet.getInt(1)+", Friend: "+resultSet.getString(3));
									toUserIds.add(resultSet.getInt(1));
								}
								else{//userID2 is the friend
									System.out.println("ID: "+resultSet.getInt(2)+", Friend: "+resultSet.getString(4));
									toUserIds.add(resultSet.getInt(2));
								}
								counter++;
							}
							
						}
						catch(Exception Ex){
							System.out.println("Error finding list of friends.  Machine Error: " +
									Ex.toString());
						}
						
						//make user select userid
						System.out.println("Enter the ID of the friend you want to message:");
						int toUserId = -1;
						try{
							toUserId = scanner.nextInt();
							scanner.nextLine();
						}
						catch(Exception Ex){
							//user did not input an int
							scanner.nextLine();
							toUserId = -1;
						}
						
						//check that selected toUserID was an option
						if(!toUserIds.contains(new Integer(toUserId))){
							toUserId = -1;
						}
						
						if(toUserId != -1){
							//read in message
							System.out.println("Enter your message:");
							String message = scanner.nextLine();
							
							//get max msgID from messages table (the msgID for this new message will be max(msgID)+1)
							int maxMsgId = -1;
							try{
								statement = connection.createStatement();
								statement.executeQuery("SET TRANSACTION READ WRITE");
								
								statement = connection.createStatement();
								query = "SELECT NVL(MAX(msgID), 0) FROM Messages";
					
								resultSet = statement.executeQuery(query);
					
								resultSet.next();
								maxMsgId = resultSet.getInt(1);
								
							}
							catch(Exception Ex){
								System.out.println("Error getting max msgID.  Machine Error: " +
										Ex.toString());
							}
							
							//insert into the Messages table
							try{
								query = "INSERT INTO Messages VALUES(?, ?, ?, ?, NULL, CURRENT_DATE)";
								preparedStatement = connection.prepareStatement(query);
								preparedStatement.setInt(1, maxMsgId+1);
								preparedStatement.setInt(2, currentUserId);
								preparedStatement.setString(3, message);
								preparedStatement.setInt(4, toUserId);
					
								resultSet = preparedStatement.executeQuery();
								
								statement = connection.createStatement();
								statement.executeQuery("COMMIT");
							}
							catch(Exception Ex){
								System.out.println("Error inserting new message.  Machine Error: " +
										Ex.toString());
							}
							
							System.out.println("Message Sent!");
							
						}
						else{
							//invalid toUserId
							System.out.println("Invalid to userID selected.");
						}
					}
					else if(messageTypeInput.compareTo("b")==0){//send to group
						//display all groups this user is a member of
						ArrayList<Integer> toGroupIds = new ArrayList<Integer>();
						try{
							statement = connection.createStatement();
							query = "SELECT g.gID, name FROM (Groups g join GroupMembership gm on g.gID=gm.gID) WHERE userID="+currentUserId;
				
							resultSet = statement.executeQuery(query);
				
							int counter = 1;
							System.out.println("Choose a Group:");
							while(resultSet.next()){
								System.out.println("ID: "+resultSet.getInt(1)+", Title: "+resultSet.getString(2));
								toGroupIds.add(resultSet.getInt(1));
								counter++;
							}
							
						}
						catch(Exception Ex){
							System.out.println("Error finding list of groups.  Machine Error: " +
									Ex.toString());
						}
						
						//make user select groupId
						System.out.println("Enter the ID of the group you want to message:");
						int toGroupId = -1;
						try{
							toGroupId = scanner.nextInt();
							scanner.nextLine();
						}
						catch(Exception Ex){
							//user did not input an int
							scanner.nextLine();
							toGroupId = -1;
						}
						
						//check that selected toGroupID was an option
						if(!toGroupIds.contains(new Integer(toGroupId))){
							toGroupId = -1;
						}
						
						if(toGroupId != -1){
							//read in message
							System.out.println("Enter your message:");
							String message = scanner.nextLine();
							
							//get max msgID from messages table (the msgID for this new message will be max(msgID)+1)
							int maxMsgId = -1;
							try{
								statement = connection.createStatement();
								statement.executeQuery("SET TRANSACTION READ WRITE");
								
								statement = connection.createStatement();
								query = "SELECT NVL(MAX(msgID), 0) FROM Messages";
					
								resultSet = statement.executeQuery(query);
					
								resultSet.next();
								maxMsgId = resultSet.getInt(1);
								
							}
							catch(Exception Ex){
								System.out.println("Error getting max msgID.  Machine Error: " +
										Ex.toString());
							}
						
							//insert into the Messages table
							try{
								statement = connection.createStatement();
								query = "INSERT INTO Messages VALUES("+(maxMsgId+1)+", "+currentUserId+", \'"+message+"\', NULL, "+toGroupId+", CURRENT_DATE)";
					
								resultSet = statement.executeQuery(query);
								
								statement = connection.createStatement();
								statement.executeQuery("COMMIT");
							}
							catch(Exception Ex){
								System.out.println("Error inserting new message.  Machine Error: " +
										Ex.toString());
							}
							
							System.out.println("Message Sent!");
						}
						else{
							//invalid toUserId
							System.out.println("Invalid to groupID selected.");
						}
					}
					else{
						//invalid input
						System.out.println("Invalid message type.");
					}
					
				}

				else if(userInput == 4){ //insert Add Friend task code here
					//find friends so we can't add twice
					ArrayList<Integer> oldfriends = new ArrayList<Integer>();
					try{
						statement = connection.createStatement();
						query = "SELECT userID1, userID2, a.name, b.name FROM ((Friends f join Profile a on f.userID1=a.userID) join Profile b on f.userID2=b.userID) WHERE userID1="+currentUserId+" OR userID2="+currentUserId;
				
						resultSet = statement.executeQuery(query);
						
						while(resultSet.next()){
							if(resultSet.getInt(1)!=currentUserId){//userID1 is the friend
								oldfriends.add(resultSet.getInt(1));
							}
							else{//userID2 is the friend
								oldfriends.add(resultSet.getInt(2));
							}
						}
							
					}
					catch(Exception Ex){
						System.out.println("Error finding friends.  Machine Error: " +
								Ex.toString());
					}

					//show numbered list of all users in the system
					ArrayList<Integer> toUserIds = new ArrayList<Integer>();
					try {
						statement = connection.createStatement();
						query = "SELECT userID, name FROM profile where userID !="+currentUserId+" ORDER BY userID DESC";

						System.out.println("Users :  ");
						resultSet = statement.executeQuery(query);
						while (resultSet.next()) {
							if (!oldfriends.contains(resultSet.getInt(1))) {
								System.out.println("ID:  " + resultSet.getString(1) + " Name:  " + resultSet.getString(2));
								toUserIds.add(new Integer(resultSet.getInt(1)));

							}
						}

					} catch(Exception Ex){
						System.out.println("Error displaying all users.  Machine Error: " +
								Ex.toString());
					}

					//prompt for ID number of user to add as friend
					
					System.out.println("Please input the ID number of the user you would like to add as a friend");
					int input = -1;
					try{
						input = scanner.nextInt();
						scanner.nextLine();
					}
					catch(Exception Ex){
						//user did not input an int
						scanner.nextLine();
						input = -1;
					}
					
					//check that selected toUserID was an option
					if(!toUserIds.contains(new Integer(input))){
						input = -1;
					}
					
					if(input != -1){

						String name = "";
						int friendid = 0;

						//display the name of the person that will be sent a friends request
						
						try {
							
							query = "SELECT userID, name FROM profile where userID = ?";
							preparedStatement = connection.prepareStatement(query); //user input we have to make a prepared statement!!!!!
							preparedStatement.setInt(1, input);
							resultSet = preparedStatement.executeQuery();
							resultSet.next();
							friendid = resultSet.getInt(1);
							name = resultSet.getString(2);
							System.out.println(name + " will be sent a friend request.");


						} catch(Exception Ex){
							System.out.println("Error retrieving user by that userID.  Machine Error: " +
									Ex.toString());
						}
						//prompt to enter a message to send with the request
						//check for SQL injection here!
						System.out.println("Please enter a message to be sent with the request.");
						String msg = scanner.nextLine();
						

						//last confirmation should be sent to user
						System.out.println("Send friend request to " + name + " with message " + msg + "?");
						while (true) {
						System.out.println("Please enter Y for yes or N for no");
						String response = scanner.nextLine();
							if (response.equals("Y") || response.equals("y")) {
								try {
									//insert tuple
									query = "INSERT INTO pendingfriends VALUES(?, ?, ?)";
									preparedStatement = connection.prepareStatement(query);
									preparedStatement.setInt(1, currentUserId);
									preparedStatement.setInt(2, friendid);
									preparedStatement.setString(3, msg);
									int result = preparedStatement.executeUpdate();
									//display success or failure
									if (result != 0) {
										System.out.println("Friend request sent successfully");
									}
									else {
										System.out.println("Friend request failed.");
									}
									break;
								} catch(Exception Ex){
										System.out.println("Failed to send friend request.  Machine Error: " +
										Ex.toString());
									}
							}
							else if (response.equals("N") || response.equals("n")) {
								break;
							}
							else {
								System.out.println("Please enter a valid input.");
							}
						}
					}
					else{
						//invalid input
						System.out.println("Invalid userID.");
					}
					System.out.println("");
				}

				else if(userInput == 5){//Display All Messages task
					
					//get all messages sent to this user and display
					try{
						statement = connection.createStatement();
						query = "SELECT email, dateSent, message FROM ((MessageRecipient mr join Messages m  on mr.msgID=m.msgID) join Profile p on fromId=p.userID) WHERE mr.userID="+currentUserId;
			
						resultSet = statement.executeQuery(query);
			
						int counter = 1;
						while(resultSet.next()){
							System.out.println("-Message "+counter+"-");
							System.out.println("From: "+resultSet.getString(1));
							System.out.println("Date Sent: "+resultSet.getDate(2));
							System.out.println(resultSet.getString(3));
							System.out.println();
							counter++;
						}
						
					}
					catch(Exception Ex){
						System.out.println("Error displaying all messages.  Machine Error: " +
								Ex.toString());
					}
					
				}
				else if(userInput == 6){//Display New Messages
					
					//get all messages sent to this user and display
					try{
						statement = connection.createStatement();
						query = "SELECT email, dateSent, message FROM ((MessageRecipient mr join Messages m  on mr.msgID=m.msgID) join Profile p on fromId=p.userID) WHERE mr.userID="+currentUserId+" AND dateSent >= TO_DATE(\'"+lastLogin+"\', \'yyyy-mm-dd\')+1";

						resultSet = statement.executeQuery(query);
			
						int counter = 1;
						while(resultSet.next()){
							System.out.println("-Message "+counter+"-");
							System.out.println("From: "+resultSet.getString(1));
							System.out.println("Date Sent: "+resultSet.getDate(2));
							System.out.println(resultSet.getString(3));
							System.out.println();
							counter++;
						}
						
					}
					catch(Exception Ex){
						System.out.println("Error displaying new messages.  Machine Error: " +
								Ex.toString());
					}
					
				}

				else if(userInput == 7){
					//insert Display Friends task code here

					//first grab all the current user's friends (and their friends) and display name and userID
					//get all friends
					ArrayList<Integer> friends = new ArrayList<Integer>();
					ArrayList<String> names = new ArrayList<String>();
					

					ArrayList<ArrayList<Integer>> friendsoffriends = new ArrayList<ArrayList<Integer>>();
					ArrayList<ArrayList<String>> names2 = new ArrayList<ArrayList<String>>();
					
					
					try{
						statement = connection.createStatement();
						query = "SELECT userID1, userID2, a.name, b.name FROM ((Friends f join Profile a on f.userID1=a.userID) join Profile b on f.userID2=b.userID) WHERE userID1="+currentUserId+" OR userID2="+currentUserId;
				
						resultSet = statement.executeQuery(query);
						
						while(resultSet.next()){
							if(resultSet.getInt(1)!=currentUserId){//userID1 is the friend
								friends.add(resultSet.getInt(1));
								names.add(resultSet.getString(3));
							}
							else{//userID2 is the friend
								friends.add(resultSet.getInt(2));
								names.add(resultSet.getString(4));
							}
							friendsoffriends.add(new ArrayList<Integer>());
							names2.add(new ArrayList<String>());
						}
							
						}
						catch(Exception Ex){
							System.out.println("Error finding friends.  Machine Error: " +
									Ex.toString());
						}
						
					//search all friends of friends, if selected profile is found degree 2
					for(int i=0; i<friends.size(); i++){
						try{
							statement = connection.createStatement();
							query = "SELECT userID1, userID2, a.name, b.name FROM ((Friends f join Profile a on f.userID1=a.userID) join Profile b on f.userID2=b.userID) WHERE userID1="+friends.get(i).intValue()+" OR userID2="+friends.get(i).intValue();
								
							resultSet = statement.executeQuery(query);
					
							while(resultSet.next()){
								if(resultSet.getInt(1)!=friends.get(i).intValue()){//userID1 is the friend
									friendsoffriends.get(i).add(resultSet.getInt(1));
									names2.get(i).add(resultSet.getString(3));
								}
								else{//userID2 is the friend
									friendsoffriends.get(i).add(resultSet.getInt(2));
									names2.get(i).add(resultSet.getString(4));
								}
							}
								
						}
						catch(Exception Ex){
							System.out.println("Error finding friends of friends.  Machine error:  " +
								Ex.toString());
						}
					}

					//we dont' want to print duplicates, so check all the friends of friends against the friends and add unique ones
					for (int i = 0; i < friendsoffriends.size(); i++) { //note this is the same as the size of friends
						for (int j = 0; j < friendsoffriends.get(i).size()-1; j++) {
							for (int k = 0; k < friends.size(); k++) {
								if (friendsoffriends.get(i).get(j) == friends.get(k)) { break;}
								if (k == friends.size()-1) {
									friends.add(friendsoffriends.get(i).get(j));
									names.add(names2.get(i).get(j));
								} //if we get all the way to the end, add it in
							} 
						}
					}

					for (int i = 0; i < friends.size(); i++) {
						System.out.println("UserID:  " + friends.get(i) + "   Name:  " + names.get(i));
					}

					//get input about which user to select
					System.out.println("Please enter the userID of the user's profile you would like to view (enter 0 to exit to main menu):  ");

					while (true) {
 						int input = scanner.nextInt();
 						scanner.nextLine();
 						if (input == 0) { break; }

						try {
							//create the query to grab the profile information
							query = "SELECT * FROM profile WHERE userID =?";
							preparedStatement = connection.prepareStatement(query);
							preparedStatement.setInt(1, input);
							resultSet = preparedStatement.executeQuery();

							//display the profile information
							while(resultSet.next()){
								System.out.println("UserID: "+resultSet.getInt(1));
								System.out.println("Name: "+resultSet.getString(2));
								System.out.println("Email: "+resultSet.getString(3));
								System.out.println("Date of birth: "+resultSet.getDate(5));
								System.out.println("Picture URL: "+resultSet.getString(6));
								System.out.println("About me: "+resultSet.getString(7));
								System.out.println("Last login: "+resultSet.getDate(8));
								System.out.println();
							}

							//prompt the user for next input
							System.out.println("PLease enter another userID to view another profile or enter 0 to exit: ");
						}
						catch(Exception Ex){
						System.out.println("Error retrieving profile.  Machine Error:  " +
								Ex.toString());
						}
					}

				}

				else if (userInput == 8){
					//insert Find User task code here
					/*
					This provides a simple search function for the system. The user should be prompted for a
					string on which to match any user in the system. Any item in the string must be matched
					against any significant feld in the profle relation. That is if the user searches for \xyz abc",
					the results should be the set of all profiles that match \xyz" union the set of all profiles that
					matches \abc".
					*/

					//store input
					ArrayList<String> results = new ArrayList<String>();

					//we want to delineate the input by spaces
					System.out.println("Please enter the search string:  ");
					String[] s = scanner.nextLine().split("\\s");
					
					ArrayList<Integer> found = new ArrayList<Integer>();
					for (int i = 0; i < s.length; i++) {
						String input = s[i];
						try {
							query = "SELECT * FROM profile WHERE name like ? or email like ? or aboutme like ?";
							preparedStatement = connection.prepareStatement(query);
							preparedStatement.setString(1, input);
							preparedStatement.setString(2, input);
							preparedStatement.setString(3, input);
							resultSet = preparedStatement.executeQuery();

							while (resultSet.next()) {
								results.add("UserID:  " + resultSet.getInt(1));
								results.add("Name:  " + resultSet.getString(2));
								results.add("Email:  "+ resultSet.getString(3));
								results.add("Date of birth:  " + resultSet.getDate(5));
								results.add("Picture url:  " + resultSet.getString(6));
								results.add("About me:  " + resultSet.getString(7));
								results.add("Last login:  " + resultSet.getDate(8));
							}
						} catch(Exception Ex){
						System.out.println("Error retrieving results of search.  Machine Error:  " +
								Ex.toString());
						}

					}

					//print out user id and name
					if (results.size() > 0) {
						System.out.println("Users found:  ");
						for (int i = 0; i < results.size(); i++) {
							System.out.println(results.get(i));
						}
					}
					System.out.println("");
				}

				else if(userInput == 9){
					//insert Confirm Friend Requests task code here
					ArrayList<Integer> toUserIds = new ArrayList<Integer>();
					try {
						//display all outstanding friend requests
						statement = connection.createStatement();
						query = "SELECT * FROM pendingfriends WHERE toID =" + currentUserId;
						resultSet = statement.executeQuery(query);
						boolean requests = false;

						while (resultSet.next()) {
							System.out.println("Request from UserID" + resultSet.getInt(1) + ":  ");
							System.out.println(resultSet.getString(3));
							System.out.println("");
							requests = true;
							toUserIds.add(resultSet.getInt(1));
						}
						
						if (requests) {
							System.out.println("Please enter the userID of the request you'd like to accept or enter ALL to accept all requests");
							String input = scanner.nextLine();
							if (input.equals("ALL") || input.equals("all")) {
								//accept all
								try {
									resultSet = statement.executeQuery(query);
									
									statement = connection.createStatement();
									statement.executeQuery("SET TRANSACTION READ WRITE");
									
									String q = "INSERT INTO Friends VALUES (?, ?, CURRENT_DATE, ?)";
									while (resultSet.next()) {
										PreparedStatement s = connection.prepareStatement(q);
										s.setInt(1, resultSet.getInt(1));
										s.setInt(2, resultSet.getInt(2));
										s.setString(3, resultSet.getString(3));
										s.executeUpdate();
									}

									System.out.println("Accepted all friend requests.");
								}catch(Exception Ex){
									System.out.println("Error finding outstanding friend requests.  Machine Error: " +
									Ex.toString());
								}
							}
							else {
								//otherwise parse as int
								int request;
								try{
									request = Integer.parseInt(input);
								}catch(NumberFormatException Ex){
									request = -1;
								}
								
								//check that selected toUserID was an option
								if(!toUserIds.contains(new Integer(request))){
									request = -1;
								}
								
								if(request != -1){
									String msg = "";
									try {
										statement = connection.createStatement();
										statement.executeQuery("SET TRANSACTION READ WRITE");
										
										query = "SELECT message FROM pendingfriends WHERE toID = ? AND fromID = ?";
										preparedStatement = connection.prepareStatement(query);
										preparedStatement.setInt(1, currentUserId);
										preparedStatement.setInt(2, request);
										resultSet = preparedStatement.executeQuery();
										resultSet.next();
										msg = resultSet.getString(1);
									}catch(Exception Ex){
										System.out.println("Error finding friend request for user "  + request + ".  Machine Error: " +
										Ex.toString());
									}


									try {
										String q = "INSERT INTO Friends VALUES (?, ?, CURRENT_DATE, ?)";
										PreparedStatement s = connection.prepareStatement(q);
										s.setInt(1, request);
										s.setInt(2, currentUserId);
										s.setString(3, msg);
										s.executeUpdate();
									}catch(Exception Ex){
										System.out.println("Error adding request into friends.  Machine Error: " +
										Ex.toString());
									}
								}
								else{
									requests = false;
								}
							}

							if(requests){
								//either way we want to delete all the pending requests
								try {
									statement = connection.createStatement();
									query = "DELETE FROM pendingfriends WHERE toID = " + currentUserId;
									statement.executeUpdate(query);
									
									statement = connection.createStatement();
									statement.executeQuery("COMMIT");

								}catch(Exception Ex){
									System.out.println("Error deleting pending requests.  Machine Error: " +
									Ex.toString());
								} 
							}
							else{
								System.out.println("invalid request to accept.");
							}
						}
						else {
							System.out.println("You have no outstanding friend requests.");
						}
					}
					catch(Exception Ex){
						System.out.println("Error finding outstanding friend requests.  Machine Error: " +
								Ex.toString());
					}
				}

				else if(userInput == 10){//Three Degrees of Separation task
					
					//show list of profiles in the system and their ids
					ArrayList<Integer> userIds = new ArrayList<Integer>();
					try{
						statement = connection.createStatement();
						query = "SELECT userID, name FROM Profile";
			
						resultSet = statement.executeQuery(query);
			
						int counter = 1;
						System.out.println("Choose a Profile:");
						while(resultSet.next()){
							System.out.println("ID: "+resultSet.getInt(1)+", Name: "+resultSet.getString(2));
							userIds.add(resultSet.getInt(1));
							counter++;
						}
						
					}
					catch(Exception Ex){
						System.out.println("Error finding list of friends.  Machine Error: " +
								Ex.toString());
					}
					
					//ask user to choose a profile id
					System.out.println("Choose an ID:");
					int userToSearchFor = -1;
					try{
						userToSearchFor = scanner.nextInt();
						scanner.nextLine();
					}
					catch(Exception Ex){
						//user did not input an int
						scanner.nextLine();
						userToSearchFor = -1;
					}
					
					//check that selected toUserID was an option
					if(!userIds.contains(new Integer(userToSearchFor))){
						userToSearchFor = -1;
					}
					
					if(userToSearchFor!=-1){
						ArrayList<Integer> degree1 = new ArrayList<Integer>();
						
						//get all friends
						try{
							statement = connection.createStatement();
							query = "SELECT userID1, userID2, a.email, b.email FROM ((Friends f join Profile a on f.userID1=a.userID) join Profile b on f.userID2=b.userID) WHERE userID1="+currentUserId+" OR userID2="+currentUserId;
				
							resultSet = statement.executeQuery(query);
				
							int counter = 1;
							while(resultSet.next()){
								if(resultSet.getInt(1)!=currentUserId){//userID1 is the friend
									degree1.add(resultSet.getInt(1));
								}
								else{//userID2 is the friend
									degree1.add(resultSet.getInt(2));
								}
								counter++;
							}
							
						}
						catch(Exception Ex){
							System.out.println("Error finding list of friends degree1.  Machine Error: " +
									Ex.toString());
						}
						
						//check for degree 1
						boolean found = false;
						for(int i=0; i<degree1.size(); i++){
							if(userToSearchFor==degree1.get(i).intValue()){
								System.out.println("1st Degree Connection:");
								System.out.println(currentUserId+"->"+userToSearchFor);
								found = true;
							}
						}
						if(found){
							continue;
						}
						
						//search all friends of friends, if selected profile is found degree 2
						ArrayList<ArrayList<Integer>> degree2 = new ArrayList<ArrayList<Integer>>();
						for(int i=0; i<degree1.size(); i++){
							degree2.add(new ArrayList<Integer>());
							try{
								statement = connection.createStatement();
								query = "SELECT userID1, userID2, a.email, b.email FROM ((Friends f join Profile a on f.userID1=a.userID) join Profile b on f.userID2=b.userID) WHERE userID1="+degree1.get(i).intValue()+" OR userID2="+degree1.get(i).intValue();
								
								resultSet = statement.executeQuery(query);
					
								int counter = 1;
								while(resultSet.next()){
									if(resultSet.getInt(1)!=degree1.get(i).intValue()){//userID1 is the friend
										degree2.get(i).add(resultSet.getInt(1));
									}
									else{//userID2 is the friend
										degree2.get(i).add(resultSet.getInt(2));
									}
									counter++;
								}
								
							}
							catch(Exception Ex){
								System.out.println("Error finding list of friends degree1.  Machine Error: " +
										Ex.toString());
							}
						}
						
						//check for degree 2
						for(int i=0; i<degree1.size(); i++){
							for(int j=0; j<degree2.get(i).size(); j++){
								if(userToSearchFor==degree2.get(i).get(j).intValue()){
									System.out.println("2nd Degree Connection:");
									System.out.println(currentUserId+"->"+degree1.get(i).intValue()+"->"+userToSearchFor);
									found = true;
								}
							}
						}
						if(found){
							continue;
						}
						
						//search all friends of friends of friends, if selected profile found degree 3
						ArrayList<ArrayList<ArrayList<Integer>>> degree3 = new ArrayList<ArrayList<ArrayList<Integer>>>();
						for(int i=0; i<degree1.size(); i++){
							degree3.add(new ArrayList<ArrayList<Integer>>());
							for(int j=0; j<degree2.get(i).size(); j++){
								degree3.get(i).add(new ArrayList<Integer>());
								try{
									statement = connection.createStatement();
									query = "SELECT userID1, userID2, a.email, b.email FROM ((Friends f join Profile a on f.userID1=a.userID) join Profile b on f.userID2=b.userID) WHERE userID1="+degree2.get(i).get(j).intValue()+" OR userID2="+degree2.get(i).get(j).intValue();
						
									resultSet = statement.executeQuery(query);
						
									int counter = 1;
									while(resultSet.next()){
										if(resultSet.getInt(1)!=degree2.get(i).get(j).intValue()){//userID1 is the friend
											degree3.get(i).get(j).add(resultSet.getInt(1));
										}
										else{//userID2 is the friend
											degree3.get(i).get(j).add(resultSet.getInt(2));
										}
										counter++;
									}
									
								}
								catch(Exception Ex){
									System.out.println("Error finding list of friends degree1.  Machine Error: " +
											Ex.toString());
								}
							}
						}
						
						//check for degree 3
						for(int i=0; i<degree1.size(); i++){
							for(int j=0; j<degree2.get(i).size(); j++){
								for(int k=0; k<degree3.get(i).get(j).size(); k++){
									if(userToSearchFor==degree3.get(i).get(j).get(k).intValue()){
										System.out.println("3rd Degree Connection:");
										System.out.println(currentUserId+"->"+degree1.get(i).intValue()+"->"+degree2.get(i).get(j)+"->"+userToSearchFor);
										found = true;
									}
								}
							}
						}
						if(found){
							continue;
						}
						
						//if not found, no connection
						System.out.println("No connection to "+userToSearchFor+".");
					}
					else{
						System.out.println("Invalid user to search for.");
					}
				}
				else if(userInput == 11){
					//insert Join A Group task code here

					//add groups that user is a member of to current group list
					ArrayList<Integer> oldgroups = new ArrayList<Integer>();
					ArrayList<Integer> toGroupIDs = new ArrayList<Integer>();
					try {
						statement = connection.createStatement();
						query = "SELECT gID FROM groupMembership WHERE userID="+currentUserId;
						resultSet = statement.executeQuery(query);
						while (resultSet.next()) {
							oldgroups.add(resultSet.getInt(1));
						}
					}
					catch(Exception Ex){
						System.out.println("Error finding current group membership.  Machine Error: " +
								Ex.toString());
					}

					//display all groups in the system that the user is not a member of
					try {
						statement = connection.createStatement();
						query = "SELECT * FROM Groups";
						resultSet = statement.executeQuery(query);

						while (resultSet.next()) {
							if (!oldgroups.contains(resultSet.getInt(1))) {
								System.out.println("Group " + resultSet.getInt(1));
								System.out.println("Name:   " + resultSet.getString(2));
								System.out.println("Description:  "+ resultSet.getString(3));
								System.out.println("");
								toGroupIDs.add(resultSet.getInt(1));
							}
						}
					}
					catch(Exception Ex){
						System.out.println("Error displaying groups.  Machine Error: " +
								Ex.toString());
					}

					System.out.println("Please enter the number of the group you would like to join.");
					int input = -1;
					try{
						input = scanner.nextInt();
						scanner.nextLine();
					}
					catch(Exception Ex){
						//user did not input an int
						scanner.nextLine();
						input = -1;
					}

					if(input != -1){
						try {
							if (toGroupIDs.contains(input)) {
								query = "INSERT INTO groupMembership VALUES(?, ?)";
								PreparedStatement prepStmt = connection.prepareStatement(query);
								prepStmt.setInt(1, input);
								prepStmt.setInt(2, currentUserId);
								int result = prepStmt.executeUpdate();
								if (result == 0) {
									System.out.println("Failed to join group.");
								}
								else {
									System.out.println("You have joined group " +input+ "!");
								}
							}
							else {
								System.out.println("Invalid Group ID entered.");
							}
						}
						catch(Exception Ex){
							System.out.println("Error joining group.  Machine Error: " +
							Ex.toString());
						}
					}
					else{
						System.out.println("Invalid input.");
					}
					System.out.println("");
				}

				else if(userInput == 12){//My Statistics task
					
					//ask user for k and x value
					System.out.println("How many top friends?");
					int k = -1;
					try{
						k = scanner.nextInt();
						scanner.nextLine();
					}
					catch(Exception Ex){
						//user did not input an int
						scanner.nextLine();
						k = -1;
					}
					System.out.println("How many months into the past?");
					int x = -1;
					try{
						x = scanner.nextInt();
						scanner.nextLine();
					}
					catch(Exception Ex){
						//user did not input an int
						scanner.nextLine();
						x = -1;
					}
					
					
					//get and display top k friends who sent or received the most messages in the last x months
					try{
						statement = connection.createStatement();
						query = "SELECT name, numMsg FROM (Profile p join (((SELECT userID, NVL(numSent, 0)+NVL(numRecv, 0) as numMsg FROM ((SELECT fromID, COUNT(msgID) as numSent FROM Messages WHERE dateSent > ADD_MONTHS(CURRENT_DATE, -"+x+") GROUP BY fromID) FULL OUTER JOIN (SELECT userID, COUNT(mr.msgID) as numRecv FROM (MessageRecipient mr join Messages m on m.msgID=mr.msgID) WHERE dateSent > ADD_MONTHS(CURRENT_DATE, -"+x+") GROUP BY userID) ON userID=fromID)) join ((SELECT userID1 AS id FROM Friends where userID2="+currentUserId+") UNION (SELECT userID2 AS id FROM Friends where userID1="+currentUserId+")) on id=userID)) on id=p.userID) ORDER BY numMsg DESC";
			
						resultSet = statement.executeQuery(query);
			
						int counter = 0;
						while(resultSet.next() && counter<k){
							System.out.println("Name: "+resultSet.getString(1)+",  Message Count: "+resultSet.getInt(2));
							counter++;
						}
						
					}
					catch(Exception Ex){
						System.out.println("Error doing statistics task.  Machine Error: " +
								Ex.toString());
					}
				}
				else if(userInput == 13){//Drop Account task
					
					//get all messages sent to this user and display
					try{
						statement = connection.createStatement();
						query = "DELETE FROM Profile WHERE userID="+currentUserId;
			
						resultSet = statement.executeQuery(query);
						
					}
					catch(Exception Ex){
						System.out.println("Error dropping account.  Machine Error: " +
								Ex.toString());
					}
					
					currentUserId = -1;
				}
				else if(userInput == 14){//log out task
					currentUserId = -1;
				}
				else if(userInput == -1){
					//let the loop break
				}
				else{
					//invalid input, ask again...
					System.out.println("Invalid input!");
				}
				
			}
		
		}
		
		//close DB connection
		try{
			connection.close();
		}
		catch(Exception Ex){
			System.out.println("Close error:  " + Ex.toString());
		}

		System.out.println("Bye.");
	}

	public static void main(String args[]){
		team04 demo = new team04();
	}
}