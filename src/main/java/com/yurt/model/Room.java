package com.yurt.model;

public class Room {
    private int id;
    private String roomNumber;
    private int capacity;
    private int currentCount;
    private String type;

    public Room(int id, String roomNumber, int capacity, int currentCount, String type) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.currentCount = currentCount;
        this.type = type;
    }

    public int getId() { return id; }
    public String getRoomNumber() { return roomNumber; }
    public int getCapacity() { return capacity; }
    public int getCurrentCount() { return currentCount; }
    public String getType() { return type; }
}