package com.google.sps.classes;

import com.google.appengine.api.datastore.Entity;

public class Comment {

    private final String comment;
    private final String user;
    private final long size;
    private final long timestamp;
    private int likes;
    private long id;

    public Comment(String comment, String user, long size, int likes, long timestamp) {
        this.comment = comment;
        this.user = user;
        this.size = size;
        this.likes = likes;
        this.timestamp = timestamp;
    }

    public Comment(Entity entity) {
        user = (String) entity.getProperty("user");
        comment = (String) entity.getProperty("comment");
        size = comment.length();
        timestamp = (long) entity.getProperty("time");
        likes = ((Long) entity.getProperty("likes")).intValue();
        
        id = entity.getKey().getId();
    }

    public Entity toEntity() {
        Entity entity = new Entity("Comment");
        
        entity.setProperty("user", user);
        entity.setProperty("comment", comment);
        entity.setProperty("time", timestamp);
        entity.setProperty("size", size);
        entity.setProperty("likes", likes);
        
        id = entity.getKey().getId();

        return entity;
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
