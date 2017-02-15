package com.jokrapp.server.data.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.lightcouch.Attachment;

/**
 * Created by pat on 10/27/2015.
 */
public class User {
    private String _id;
    private String _rev;
    private String deviceId;
    private String deviceOS; // a: android, i: ios
    private String userName;
    private String languages; // languages, space separated with beginning and ending space :-)
    private int yearOfBirth;
	private Long creationTimestamp;
	private Long lastActiveTimestamp;
    private Long listeningTimestamp; // timestamp of the last joke the user was listening
    private String objectClass = "user";
    
	private HashMap<String, Attachment> _attachments;
	
	private ArrayList<String> favoriteJokes;
	private ArrayList<String> followingUsers;
	private ArrayList<String> blockedUsers;
	private ArrayList<String> likes;
	private ArrayList<String> dislikes;
	
	private int followersCnt;
	private int jokesCnt;
	
	private boolean isAdmin = false;
	
	public User(){
    	super();
    }
    		
    public User(String id, String deviceOS, String userName) {
        this._id = id;
        this.deviceOS = deviceOS;
        this.userName = userName;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String id) {
        this._id = id;
    }

    public String getDeviceOS() {
        return deviceOS;
    }

    public void setDeviceOS(String deviceOS) {
        this.deviceOS = deviceOS;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(int yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Long getCreationTimestamp() {
		return creationTimestamp;
	}

	public void setCreationTimestamp(Long creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public Long getListeningTimestamp() {
		return listeningTimestamp;
	}

	public void setListeningTimestamp(Long listeningTimestamp) {
		this.listeningTimestamp = listeningTimestamp;
	}

    public String getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(String objectClass) {
		this.objectClass = objectClass;
	}

	public Long getLastActiveTimestamp() {
		return lastActiveTimestamp;
	}

	public void setLastActiveTimestamp(Long lastActiveTimestamp) {
		this.lastActiveTimestamp = lastActiveTimestamp;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public ArrayList<String> getFavoriteJokes() {
		return favoriteJokes;
	}

	public void setFavoriteJokes(ArrayList<String> favoriteJokes) {
		this.favoriteJokes = favoriteJokes;
	}

	public ArrayList<String> getFollowingUsers() {
		return followingUsers;
	}

	public void setFollowingUsers(ArrayList<String> followingUsers) {
		this.followingUsers = followingUsers;
	}

	public String get_rev() {
		return _rev;
	}

	public void set_rev(String _rev) {
		this._rev = _rev;
	}

	public HashMap<String, Attachment> get_attachments() {
		return _attachments;
	}

	public void set_attachments(HashMap<String, Attachment> _attachments) {
		this._attachments = _attachments;
	}

	public String getLanguages() {
		return languages;
	}

	public void setLanguages(String languages) {
		this.languages = languages;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public ArrayList<String> getBlockedUsers() {
		return blockedUsers;
	}

	public void setBlockedUsers(ArrayList<String> blockedUsers) {
		this.blockedUsers = blockedUsers;
	}

	public ArrayList<String> getLikes() {
		return likes;
	}

	public void setLikes(ArrayList<String> likes) {
		this.likes = likes;
	}

	public ArrayList<String> getDislikes() {
		return dislikes;
	}

	public void setDislikes(ArrayList<String> dislikes) {
		this.dislikes = dislikes;
	}

	public int getFollowersCnt() {
		return followersCnt;
	}

	public void setFollowersCnt(int followersCnt) {
		this.followersCnt = followersCnt;
	}

	public int getJokesCnt() {
		return jokesCnt;
	}

	public void setJokesCnt(int jokesCnt) {
		this.jokesCnt = jokesCnt;
	}

}
