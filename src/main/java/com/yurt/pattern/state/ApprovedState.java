package com.yurt.pattern.state;

public class ApprovedState implements LeaveState {
    @Override
    public String getStatusName() {
        return "Onaylandı";
    }

    @Override
    public String getDisplayColor() {
        return "#4CAF50";
    }
}