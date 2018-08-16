package twitter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * The object which represents a user
 * @author danieldluyz
 */
public class TwitterUser {

	/**
	 * The id of the user
	 */
	private int id;

	/**
	 * The accounts followed by that user
	 */
	private ArrayList<TwitterUser> friends;

	/**
	 * The user's name
	 */
	private String name;

	/**
	 * The user's screen name
	 */
	private String screenName;

	/**
	 * The number of followers of the user
	 */
	private int numberOfFollowers;

	/**
	 * The number of tweets of the user which have been marked as favorites
	 */
	private int favouritesCount;

	/*
	 * CONSTRUCTOR
	 */

	/**
	 * This method creates a user
	 * @param id The id of the user
	 * @param n The name of the user
	 * @param sn The screen name of the user
	 */
	public TwitterUser(int id, String n, String sn) {
		this.id = id;
		this.name = n;
		this.screenName = sn;
		this.friends = new ArrayList<TwitterUser>();
		this.numberOfFollowers = -1;
		this.favouritesCount = 0;
	}

	/*
	 * GETTERS AND SETTERS
	 */

	/**
	 * This methods sets the id of a user
	 * @param id The id to be set
	 */
	public void setIds(int id) {
		this.id = id;
	}

	/**
	 * This method sets the number of followers of a user
	 * @param nF The number of followers of the user
	 */
	public void setNumberOfFollowers(int nF) {
		this.numberOfFollowers = nF;
	}

	/**
	 * This method sets the number of tweets marked as favorites of a user
	 * @param nF The number of tweets marked as favorites of a user
	 */
	public void setFavouritesCount(int fC) {
		this.favouritesCount = fC;
	}

	/**
	 * This method adds a friend the the user
	 * @param nF The user's new friend
	 */
	public void addFriend(TwitterUser friend) {
		this.friends.add(friend);
	}

	/**
	 * This method returns the id of the user
	 * @return The user's id
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * This method returns the list of friends of the user
	 * @return The list of friends of the user
	 */
	public ArrayList<TwitterUser> getFriends() {
		return this.friends;
	}

	/**
	 * This method returns the name of the user
	 * @return The user's name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * This method returns the screen name of the user
	 * @return The user's screen name
	 */
	public String getScreenName() {
		return this.screenName;
	}

	/**
	 * This method returns the number of followers of the user
	 * @return The user's number of followers
	 */
	public int getNumberOfFollowers() {
		return this.numberOfFollowers;
	}

	/**
	 * This method returns the number of tweets marked as favorite of a user
	 * @return The user's favorite count
	 */
	public int getFavouritesCount() {
		return this.favouritesCount;
	}	

	/*
	 * PRINTING ALGORITHMS
	 */

	/**
	 * This method prints the information of the user
	 * @param pw The PrintWriter which is used to write the info
	 * @param parent The account followed by AntHormigas which follows this user
	 */
	public void print(PrintWriter pw, String parent) {
		for (int i = 0; i < friends.size(); i++) {
			pw.println(parent + "." + this.screenName + "." + friends.get(i).getScreenName() + "," + friends.get(i).numberOfFollowers + "," + friends.get(i).getFavouritesCount());
		}
	}

	/**
	 * This method prints the top X friends of the user with the most followers
	 * @param pw The PrintWriter which is used to write the info
	 * @param parent The account followed by AntHormigas which follows this user
	 * @param max The number which specifies how many users to be printed
	 */
	public void printByNumberOfFollowers(PrintWriter pw, String parent, int max) {
		orderFriendsByNumberOfFollowers(0, friends.size() - 1);
		for (int i = friends.size() - 9; i >= Math.max(0, friends.size() - max - 9); i--) {
			pw.println(parent + "." + this.screenName + "." + friends.get(i).getScreenName() + "," + friends.get(i).numberOfFollowers);
		}
	}

	/**
	 * This method prints the top X friends of the user with the most favorite count
	 * @param pw The PrintWriter which is used to write the info
	 * @param parent The account followed by AntHormigas which follows this user
	 * @param max The number which specifies how many users to be printed
	 */
	public void printTopFriendsByFavouriteCount(PrintWriter pw, String parent, int max) {
		orderFriendsByFavouritesCount(0, friends.size() - 1);
		for (int i = friends.size() - 9; i >= Math.max(0, friends.size() - max - 9); i--) {
			pw.println(parent + "." + this.screenName + "." + friends.get(i).getScreenName() + "," + friends.get(i).numberOfFollowers);
		}
	}

	/*
	 * SORTING ALGORITHMS
	 */

	/**
	 * Implementation of the quick sort algorithm taken from http://www.java2novice.com/java-sorting-algorithms/quick-sort/
	 * It sorts the user's friends based on their number of followers
	 * @param lowerIndex The starting index of the sort
	 * @param higherIndex The ending index of the sort
	 */
	private void orderFriendsByNumberOfFollowers(int lowerIndex, int higherIndex) {
		int i = lowerIndex;
		int j = higherIndex;
		// calculate pivot number, I am taking pivot as middle index number
		TwitterUser pivot = friends.get(lowerIndex+(higherIndex-lowerIndex)/2);
		// Divide into two arrays
		while (i <= j) {
			/**
			 * In each iteration, we will identify a number from left side which 
			 * is greater then the pivot value, and also we will identify a number 
			 * from right side which is less then the pivot value. Once the search 
			 * is done, then we exchange both numbers.
			 */
			while (friends.get(i).getNumberOfFollowers() < pivot.getNumberOfFollowers()) {
				i++;
			}
			while (friends.get(j).getNumberOfFollowers() > pivot.getNumberOfFollowers()) {
				j--;
			}
			if (i <= j) {
				exchangePositions(i, j);
				//move index to next position on both sides
				i++;
				j--;
			}
		}
		// call quickSort() method recursively
		if (lowerIndex < j)
			orderFriendsByNumberOfFollowers(lowerIndex, j);
		if (i < higherIndex)
			orderFriendsByNumberOfFollowers(i, higherIndex);
	}

	/**
	 * Implementation of the quick sort algorithm taken from http://www.java2novice.com/java-sorting-algorithms/quick-sort/
	 * It sorts the user's friends based on their favorites count
	 * @param lowerIndex The starting index of the sort
	 * @param higherIndex The ending index of the sort
	 */
	private void orderFriendsByFavouritesCount(int lowerIndex, int higherIndex) {
		int i = lowerIndex;
		int j = higherIndex;
		// calculate pivot number, I am taking pivot as middle index number
		TwitterUser pivot = friends.get(lowerIndex+(higherIndex-lowerIndex)/2);
		// Divide into two arrays
		while (i <= j) {
			/**
			 * In each iteration, we will identify a number from left side which 
			 * is greater then the pivot value, and also we will identify a number 
			 * from right side which is less then the pivot value. Once the search 
			 * is done, then we exchange both numbers.
			 */
			while (friends.get(i).getFavouritesCount() < pivot.getFavouritesCount()) {
				i++;
			}
			while (friends.get(j).getFavouritesCount() > pivot.getFavouritesCount()) {
				j--;
			}
			if (i <= j) {
				exchangePositions(i, j);
				//move index to next position on both sides
				i++;
				j--;
			}
		}
		// call quickSort() method recursively
		if (lowerIndex < j)
			orderFriendsByNumberOfFollowers(lowerIndex, j);
		if (i < higherIndex)
			orderFriendsByNumberOfFollowers(i, higherIndex);
	}

	/**
	 * Method which is used to swap the position of two users when ordering them
	 * @param i The position of one of the users to be swapped 
	 * @param j The position of the other user to be swapped
	 */
	private void exchangePositions(int i, int j) {
		Collections.swap(friends, i, j);
	}

}
