package com.yurt.pattern.state;

public class PendingState implements LeaveState {
    @Override
    public String getStatusName() {
        return "Beklemede";
    }

    @Override
    public String getDisplayColor() {
        return "#FFA500";
    }
}