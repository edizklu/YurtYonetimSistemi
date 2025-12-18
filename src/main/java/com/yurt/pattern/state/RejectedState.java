package com.yurt.pattern.state;

public class RejectedState implements LeaveState {
    @Override
    public String getStatusName() {
        return "Reddedildi";
    }

    @Override
    public String getDisplayColor() {
        return "#F44336";
    }
}