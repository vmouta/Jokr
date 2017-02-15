package com.jokrapp.android.data.model;

import java.util.ArrayList;

/**
 * Created by pat on 10/27/2015.
 */
public class User {
    private String _id;
    private String deviceId;
    private String deviceOS; // a: android, i: ios
    private String userName;
    private String languages; // languages, space separated

    private int yearOfBirth;
	private Long creationTimestamp;
	private Long lastActiveTimestamp;
    private Long listeningTimestamp; // timestamp of the last joke the user was listening
    private String objectClass = "user";
    private int followersCnt;

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

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
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

    public int getFollowersCnt() {
        return followersCnt;
    }

    public void setFollowersCnt(int followersCnt) {
        this.followersCnt = followersCnt;
    }

    @Override
    public String toString() {
        return "User{" +
                "_id='" + _id + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", deviceOS='" + deviceOS + '\'' +
                ", userName='" + userName + '\'' +
                ", languages='" + languages + '\'' +
                ", yearOfBirth=" + yearOfBirth +
                ", creationTimestamp=" + creationTimestamp +
                ", lastActiveTimestamp=" + lastActiveTimestamp +
                ", listeningTimestamp=" + listeningTimestamp +
                ", objectClass='" + objectClass + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return _id.equals(user._id);

    }

    @Override
    public int hashCode() {
        return _id.hashCode();
    }
}
