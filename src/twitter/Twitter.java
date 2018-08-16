package twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

/**
 * A Twitter information retrieval project used in a research of Facultad de Educación at Universidad de los Andes
 * @author danieldluyz
 */
public class Twitter {

	/**
	 * The CONSUMER KEY from the Twitter developer account
	 */
	private final static String CONSUMER_KEY = "Zfr5Ru6eu9PejAEBF1cNTwrwd";

	/**
	 * The CONSUMER SECRET from the Twitter developer account
	 */
	private final static String CONSUMER_SECRET = "R8z4zBaeZKpgrYBYZcUhCipf91nZzzhgTH48JCX4PMPzyjAjA8";

	/**
	 * The ACCESS TOKEN from the Twitter developer account
	 */
	private final static String ACCESS_TOKEN = "963446309880463361-8wowEV6XYbsiDHVj1gVDjjrfOUUuuto";

	/**
	 * The ACCESS TOKEN SECRET from the Twitter developer account
	 */
	private final static String ACCESS_TOKEN_SECRET = "BK0cPTJyAlbFyc8PDL1CQCB6j1EJHoyqfzZheEZy0bDyL";
	
	/**
	 * The earliest date from which to recover the tweets
	 */
	private final static LocalDate MAX_DATE = LocalDate.parse("2018-01-01");
	
	/**
	 * The max number of tweets per user that we want to obtain
	 */
	private final static int numberOfTweets = 10000;

	/**
	 * The location of the file where the information of the users is stored
	 */
	private final static String USERS_FILE = "./data/users.txt";

	/**
	 * The location of the file where the information of the actors in the format required by Gephi is printed
	 */
	private final static String ACTORS_FILE = "./data/actorsFile.csv";

	/**
	 * The location of the file where the information of the ties in the format required by Gephi is printed
	 */
	private final static String TIES_FILE = "./data/tiesFile.csv";
	
	/**
	 * The location of the file where the information of the ties in the format required by Gephi is printed
	 */
	//private final static String TWEETS_FILE = "./data/tweets.txt";

	/**
	 * The twitter instance which helps to do the requests
	 */
	private twitter4j.Twitter twitter;

	/**
	 * The array where the users are first stored
	 */
	private ArrayList<TwitterUser> twitterUsers;

	/**
	 * The last friend who was checked when obtaining the list of users followed by an account.
	 * It helps to track the position because of the API restriction. 
	 */
	private int friend;

	/**
	 * The last friend of a friend who was obtained when retrieving the information of users followed by an account.
	 * It helps to track the status of the retrieval of information because of the API restriction. 
	 */
	private int friendOfFriend;

	/**
	 * The next cursor to be checked (for friends who follow more than 5.000 accounts)
	 */
	private long globalCursor;

	/**
	 * The HashMap where the users are loaded after all their information has been retrieved
	 */
	private LinkedHashMap<String, TwitterUser> usersHash;

	//Constructor

	/**
	 * Constructor
	 */
	public Twitter() {
		setUpConfiguration();
	}

	//Set up methods

	/**
	 * This method helps to set up the configuration needed in order to connect to the twitter API
	 */
	public void setUpConfiguration() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey(CONSUMER_KEY)
		.setOAuthConsumerSecret(CONSUMER_SECRET)
		.setOAuthAccessToken(ACCESS_TOKEN)
		.setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET)
		.setTweetModeExtended(true);
		TwitterFactory tf = new TwitterFactory(cb.build());
		this.twitter = tf.getInstance();
		this.twitterUsers = new ArrayList<TwitterUser>();
		this.friend = -1;
		this.friendOfFriend = -1;
		this.globalCursor = -1;
		usersHash = new LinkedHashMap<String, TwitterUser>();
	}

	/**
	 * This method loads the information of the users from the file where their information is stored after it's been retrieved
	 * @throws Exception Any exception which leads to an error
	 */
	public void loadFromFile() throws Exception {
		BufferedReader bf = new BufferedReader(new FileReader(new File("./data/users.txt")));
		String line = bf.readLine();
		line = bf.readLine();
		line = bf.readLine();
		while(line != null) {
			String[] data = line.split("\\.");
			if(data.length == 2) {
				String screenName = data[1].split(",")[0];
				TwitterUser user = new TwitterUser(-1, "", screenName);
				twitterUsers.add(user);
				this.friend += 1;
			} else if (data.length == 3) {
				String[] friendData = data[2].split(",");
				TwitterUser newFriend = new TwitterUser(-1, "", friendData[0]);
				newFriend.setNumberOfFollowers(Integer.parseInt(friendData[1]));
				newFriend.setFavouritesCount(Integer.parseInt(friendData[2]));
				twitterUsers.get(this.friend).addFriend(newFriend);
			}
			line = bf.readLine();
		}
		for (int i = 0; i < twitterUsers.size(); i++) {
			System.out.println(twitterUsers.get(i).getScreenName() + " - " + twitterUsers.get(i).getFriends().size());
		}
		bf.close();
	}

	//Logic methods

	/**
	 * The main method in order to retrieve the information of the accounts which are followed by AntHormigas
	 * It retrieves at first the list of accounts followed by AntHormigas
	 * For each of these accounts, it then retrieves the list of the users which they follow along with data such as the number of followers
	 * and favorites of those accounts.
	 * @throws Exception
	 */
	public void getTopFriends() throws Exception {
		System.out.println("**************************");
		System.out.println("TFNF - Looking for the users which AntHormigas follows");
		long cursor = -1;
		List<User> users = twitter.getFriendsList(twitter.getId(), cursor);
		System.out.println("AntHormigas follows (is friends with) " + users.size() + " users in total");
		for (int i = 0; i < users.size(); i++) {
			System.out.println("Friend " + i + ": " + users.get(i).getName());
			TwitterUser newUser = new TwitterUser(1, users.get(i).getName(), users.get(i).getScreenName());
			newUser.setNumberOfFollowers(users.get(i).getFollowersCount());
			System.out.println("Looking for the list of friends of this friend");
			long newCursor = -1;
			IDs userFriends = twitter.getFriendsIDs(newUser.getScreenName(), newCursor);
			System.out.println(newUser.getScreenName() + " follows " + userFriends.getIDs().length + " users in total");
			this.friend = i;
			for (int j = 0; j < userFriends.getIDs().length; j++) {
				this.friendOfFriend = j;
				long id = userFriends.getIDs()[j];
				User friend = twitter.showUser(id);
				int numberOfFollowers = friend.getFollowersCount();
				int favouritesCount = friend.getFavouritesCount();
				TwitterUser newFriend = new TwitterUser(1, friend.getName(), friend.getScreenName());
				newFriend.setNumberOfFollowers(numberOfFollowers);
				newFriend.setFavouritesCount(favouritesCount);
				newUser.addFriend(newFriend);
				System.out.println("ant." + newUser.getScreenName() + "." + friend.getScreenName() + "," + newFriend.getNumberOfFollowers() + "," + newFriend.getFavouritesCount());
				this.globalCursor = userFriends.getNextCursor();
			}
			twitterUsers.add(newUser);
			printTwitterUser(twitterUsers.indexOf(newUser));
		}
		System.out.println("TFNF - We're done looking for the users which AntHormigas follows");
		System.out.println("**************************");
	}

	/**
	 * This method prints the information needed by Gephi in order to create the graph
	 * @throws Exception Any exception which leads to an error
	 */
	public void printGephiData() throws Exception {
		loadHashTable();
		createIds();
		printActorsFile();
		printTiesFile();
	}

	/**
	 * This method loads the hash table based on the users which are stored in the array
	 * We use this hash table in order to process the information and later add an id to each of the users
	 */
	private void loadHashTable() {
		for (int i = 0; i < twitterUsers.size(); i++) {
			TwitterUser user = twitterUsers.get(i);
			String screenName = user.getScreenName();
			if(usersHash.get(screenName) == null) usersHash.put(screenName, user);
			ArrayList<TwitterUser> friends = user.getFriends();
			for (int j = 0; j < friends.size(); j++) {
				TwitterUser friend = friends.get(j);
				String friendsScreenName = friend.getScreenName();
				if(usersHash.get(friendsScreenName) == null) usersHash.put(friendsScreenName, friend);
			}
		}

	}

	/**
	 * This method sets a unique id to each of the users which are being processed
	 */
	private void createIds() {
		int id = 1;
		Set<String> keys = usersHash.keySet();
		for(String key: keys){
			TwitterUser user = usersHash.get(key);
			user.setIds(id);
			id++;
		}
	}
	
	private void retrieveTweets() throws Exception {
		System.out.println("**************************");
		System.out.println("Retrieving tweets");
		long cursor = -1;
		List<User> users = twitter.getFriendsList(twitter.getId(), cursor);
		for (int i = 14; i <= 14; i++) {
			User user = users.get(i);
			String screenName = user.getScreenName();
			System.out.println("Tweets from friend number " + i + " - "+ screenName);
			int tweetsOfUser = 0;
			Paging paging = new Paging();
			long lastId = Long.MAX_VALUE;
			int totalNumberOfTweets = user.getStatusesCount();
			PrintWriter pw = new PrintWriter(new FileWriter(new File("./data/tweets/"+screenName+".txt"), true));
			boolean keepLooking = true;
			while(tweetsOfUser <= numberOfTweets && tweetsOfUser < totalNumberOfTweets && keepLooking) {
				ResponseList<Status> tweets = twitter.getUserTimeline(user.getScreenName(), paging);
				System.out.println("Printing his tweets - The paging is " + paging.getPage() + ". This page obtained " + tweets.size() + " tweets.");
				for (int j = 0; j < tweets.size(); j++) {
					Status tweet = tweets.get(j);
					Date date = tweet.getCreatedAt();
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					LocalDate tweetDate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
					System.out.println("The date is " + tweetDate);
					if(tweetDate.isAfter(MAX_DATE)) {
						lastId = tweet.getId();
						String text = tweet.getText();
						if(tweet.isRetweet() == true) text = tweet.getRetweetedStatus().getText(); 
						text = text.replace("\n", "").replace("\r", "");
						if(text.length() > 0) {
							pw.println(screenName + "--;--" + text + "--;--" + date);
							if(tweet.getId() < lastId) lastId = tweet.getId();
							tweetsOfUser++;	
						}
					} else keepLooking = false;
				}
				paging.maxId(lastId - 1);
			}
			System.out.println("Done with his tweets");
			pw.close();
		}
		System.out.println("Tweets printed");
		System.out.println("**************");
	}

	//PRINTING METHODS

	/**
	 * This method prints all the information of all the users
	 * @throws Exception Any exception which leads to an error
	 */
	public void print() throws Exception {
		System.out.println("**************************");
		System.out.println("Printing results");
		PrintWriter pw = new PrintWriter(new FileWriter(new File(USERS_FILE), true));
		pw.println("id,value");
		pw.println("ant,");
		for (int i = 0; i < twitterUsers.size(); i++) {
			pw.println("ant." + twitterUsers.get(i).getScreenName() + ",");
			twitterUsers.get(i).print(pw, "ant");
		}
		pw.close();
		System.out.println("Finished");
		System.out.println("**************************");
	}

	/**
	 * This method prints all the information of one of an user
	 * It is used when printing the information retrieved of the users which are followed
	 * by the users followed by AntHormigas
	 * @throws Exception Any exception which leads to an error
	 */
	public void printTwitterUser(int user) throws Exception {
		System.out.println("**************************");
		System.out.println("Printing an user");
		PrintWriter pw = new PrintWriter(new FileWriter(new File(USERS_FILE), true));
		pw.println("ant." + twitterUsers.get(user).getScreenName() + ",");
		twitterUsers.get(user).print(pw, "ant");
		pw.close();
		System.out.println("Finished printing the user");
		System.out.println("**************************");
	}

	/**
	 * This method prints the top 5 users with the biggest number of followers
	 * of each of the accounts which are followed by AntHormigas
	 * @throws Exception Any exception which leads to an error
	 */
	public void printTopFriendsByNumberOfFollowers() throws Exception {

		System.out.println("**************************");
		System.out.println("Printing results - Most Followers");
		PrintWriter pw = new PrintWriter(new FileWriter(new File("./data/mostFollowersView.txt"), false));
		for (int i = 0; i < twitterUsers.size(); i++) {
			pw.println("ant." + twitterUsers.get(i).getScreenName() + ",");
			twitterUsers.get(i).printByNumberOfFollowers(pw, "ant", 5);
		}
		pw.close();
		System.out.println("Finished");
		System.out.println("**************************");
	}

	/**
	 * This method prints the top 5 users with the biggest number of favorites
	 * of each of the accounts which are followed by AntHormigas
	 * @throws Exception Any exception which leads to an error
	 */
	public void printTopFriendsByFavouriteCount() throws Exception {

		System.out.println("**************************");
		System.out.println("Printing results - Favourite count");
		PrintWriter pw = new PrintWriter(new FileWriter(new File("./data/mostFavouritesView.txt"), false));
		for (int i = 0; i < twitterUsers.size(); i++) {
			pw.println("ant." + twitterUsers.get(i).getScreenName() + ",");
			twitterUsers.get(i).printTopFriendsByFavouriteCount(pw, "ant", 5);
		}
		pw.close();
		System.out.println("Finished");
		System.out.println("**************************");
	}

	/**
	 * This method prints the file with the information of every actor as required by Gephi
	 * @throws Exception Any exception which leads to an error
	 */
	public void printActorsFile() throws Exception {
		System.out.println("**************************");
		System.out.println("Printing the actor's file");
		PrintWriter pw = new PrintWriter(new FileWriter(new File(ACTORS_FILE), false));
		pw.println("id,name,followers,favourites");
		Set<String> keys = usersHash.keySet();
		for(String key: keys){
			TwitterUser user = usersHash.get(key);
			pw.println(user.getId() + "," + user.getScreenName() + "," + user.getNumberOfFollowers() + "," + user.getFavouritesCount());
		}
		pw.close();
		System.out.println("Finished");
		System.out.println("**************************");
	}

	/**
	 * This method prints the file with the information of every tie as required by Gephi
	 * @throws Exception Any exception which leads to an error
	 */
	public void printTiesFile() throws Exception {
		System.out.println("**************************");
		System.out.println("Printing the ties' file");
		PrintWriter pw = new PrintWriter(new FileWriter(new File(TIES_FILE), false));
		pw.println("source,target");
		for (int i = 0; i < twitterUsers.size(); i++) {
			TwitterUser user = twitterUsers.get(i);
			ArrayList<TwitterUser> friends = user.getFriends();
			String screenName = user.getScreenName();
			int sourceId = usersHash.get(screenName).getId();
			for (int j = 0; j < friends.size(); j++) {
				TwitterUser friend = friends.get(j);
				String friendsScreenName = friend.getScreenName();
				int targetId = usersHash.get(friendsScreenName).getId();
				pw.println(sourceId + "," + targetId);
			}
		}
		pw.close();
		System.out.println("Finished");
		System.out.println("**************************");
	}

	/**
	 * This method prints the information of the last friend and friend of friend which was checked before the Twitter API stopped
	 * responding because of the limit of the account
	 */
	public void printLastChecked() {
		System.out.println("Last friend checked " + this.friend);
		System.out.println("Last friend of friend checked " + this.friendOfFriend);
		System.out.println("Last cursor " + this.globalCursor);
	}

	//MAIN

	/**
	 * The method which sets up the execution of the code
	 * @param args
	 * @throws TwitterException
	 */
	public static void main(String[] args) throws TwitterException {

		Twitter t = new Twitter();

		try {

//			t.loadFromFile();
//			t.printTopFriendsByNumberOfFollowers();
//			t.printTopFriendsByFavouriteCount();
//			t.printGephiData();

//			t.getTopFriendsByNumberOfFollowers();
//			t.printTopFriendsByNumberOfFollowers();
//			t.printTopFriendsByFavouriteCount();
//			t.printLastChecked();
			
			t.retrieveTweets();

		} catch (Exception e) {
			try {

				//t.printTopFriendsByNumberOfFollowers();
				e.printStackTrace();
				//t.printLastChecked();


			} catch (Exception e1) {
				//e1.printStackTrace();
				//t.printLastChecked();
			}
		}
	}
}
