package com.yurt.pattern;

import com.yurt.model.User;
import com.yurt.model.Student;
import com.yurt.model.Staff;

public class UserFactory {
    public static User createUser(String role, int id, String username, String password) {
        if (role.equalsIgnoreCase("STUDENT")) {
            return new Student(id, username, password, "Öğrenci", "Test", "11111", 0);
        } else if (role.equalsIgnoreCase("STAFF")) {
            return new Staff(id, username, password, "Personel", "Test");
        }
        return null;
    }
}