package com.jokrapp.android.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pat on 10/25/2015.
 */
public class Joke {

    private String _id;
    private String title;
    private String tags;
    private int likes;
    private String language; // ISO 639-1 codes (en, de, fr) and "sde" swiss german see https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes

    @SerializedName("public")
    private boolean isPublic = false;

    private int validationState; // 0: not yet validated, 1: validation OK, 2: validation is not OK (bad joke)


    @SerializedName("uploaded")
    private boolean isUploaded = false;
    private int framingBegin = 0;
    private int framingEnd = 0;
    private long creationTimestamp;
    private long uploadTimestamp;
    private String objectClass = "joke";
    private String userName;
    private String userId;

    public Joke() {
        super();
    }

    public Joke(String _id, String title) {
        this._id = _id;
        this.title = title;
    }

    public Joke(String _id, String title, String tags) {
        this._id = _id;
        this.title = title;
        this.tags = tags;

    }

    public Joke(String _id, String title, String tags, String language) {
        this._id = _id;
        this.title = title;
        this.tags = tags;
        this.language = language;
    }

    public Joke(String _id, String title, String tags, int likes, String language, boolean isPublic) {
        this._id = _id;
        this.title = title;
        this.tags = tags;
        this.likes = likes;
        this.language = language;
        this.isPublic = isPublic;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getLanguage() {
        return language;
    }

    public int getValidationState() {
        return validationState;
    }

    public void setValidationState(int validationState) {
        this.validationState = validationState;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Joke joke = (Joke) o;

        return _id.equals(joke._id);

    }

    @Override
    public int hashCode() {
        return _id.hashCode();
    }

    public int getFramingBegin() {
        return framingBegin;
    }

    public void setFramingBegin(int framingBegin) {
        this.framingBegin = framingBegin;
    }

    public int getFramingEnd() {
        return framingEnd;
    }

    public void setFramingEnd(int framingEnd) {
        this.framingEnd = framingEnd;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public long getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(long uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(String objectClass) {
        this.objectClass = objectClass;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setIsUploaded(boolean isUploaded) {
        this.isUploaded = isUploaded;
    }


    @Override
    public String toString() {
        return "Joke{" +
                "_id='" + _id + '\'' +
                ", title='" + title + '\'' +
                ", tags='" + tags + '\'' +
                ", likes=" + likes +
                ", language='" + language + '\'' +
                ", isPublic=" + isPublic +
                ", isUploaded=" + isUploaded +
                ", framingBegin=" + framingBegin +
                ", framingEnd=" + framingEnd +
                ", creationTimestamp=" + creationTimestamp +
                ", uploadTimestamp=" + uploadTimestamp +
                ", objectClass='" + objectClass + '\'' +
                ", userName='" + userName + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
