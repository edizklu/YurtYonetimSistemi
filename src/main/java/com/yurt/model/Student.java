package com.yurt.model;

public class Student extends User {
    private String name;
    private String surname;
    private String tcNo;
    private int roomId;

    public Student(int id, String username, String password, String name, String surname, String tcNo, int roomId) {
        super(id, username, password);
        this.name = name;
        this.surname = surname;
        this.tcNo = tcNo;
        this.roomId = roomId;
    }

    @Override
    public String getRole() {
        return "STUDENT";
    }

    public String getName() { return name; }
    public String getSurname() { return surname; }
}