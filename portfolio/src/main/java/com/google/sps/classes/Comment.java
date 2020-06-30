package com.google.sps.classes;

public class Comment {

    private final String comment;
    private final String user;
    private final long size;
    private final long timestamp;
    private int likes;
    private final long id;

    public Comment(String comment, String user, long size, int likes, long timestamp, long id) {
        this.comment = comment;
        this.user = user;
        this.size = size;
        this.likes = likes;
        this.timestamp = timestamp;
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public String getUser() {
        return user;
    }

    public long getSize() {
        return size;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public long getId() {
        return id;
    }

}
