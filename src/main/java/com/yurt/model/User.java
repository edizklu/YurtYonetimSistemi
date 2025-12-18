package com.yurt.model;

public abstract class User {
    protected int id;
    protected String username;
    protected String password;

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public abstract String getRole();

    public int getId() { return id; }
    public String getUsername() { return username; }
}