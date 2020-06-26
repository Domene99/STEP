package com.google.sps.classes;

public class Comment {

    private final String comment;
    private final String user;
    private final long size;
    private int likes;

    public Comment(String comment, String user, long size, int likes) {
        this.comment = comment;
        this.user = user;
        this.size = size;
        this.likes = likes;
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
}
