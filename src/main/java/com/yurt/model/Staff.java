package com.yurt.model;

public class Staff extends User {
    private String name;
    private String surname;

    public Staff(int id, String username, String password, String name, String surname) {
        super(id, username, password);
        this.name = name;
        this.surname = surname;
    }

    @Override
    public String getRole() {
        return "STAFF";
    }

    public String getName() { return name; }
}