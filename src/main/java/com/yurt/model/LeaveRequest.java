package com.yurt.model;

import com.yurt.pattern.observer.Observer;
import com.yurt.pattern.observer.Subject;
import com.yurt.pattern.state.ApprovedState;
import com.yurt.pattern.state.LeaveState;
import com.yurt.pattern.state.PendingState;
import com.yurt.pattern.state.RejectedState;

import java.util.ArrayList;
import java.util.List;

public class LeaveRequest implements Subject {
    private int id;
    private int studentId;
    private String startDate;
    private String endDate;
    private String reason;
    private LeaveState state;

    private List<Observer> observers = new ArrayList<>();

    public LeaveRequest(int id, int studentId, String startDate, String endDate, String reason, String statusStr) {
        this.id = id;
        this.studentId = studentId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;

        switch (statusStr) {
            case "Onaylandı": this.state = new ApprovedState(); break;
            case "Reddedildi": this.state = new RejectedState(); break;
            default: this.state = new PendingState(); break;
        }
    }

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    public void setStatus(String newStatus) {
        notifyObservers("Talep ID " + id + " durumu değişti: " + newStatus);
    }

    public String getStatus() { return state.getStatusName(); }
    public int getId() { return id; }
    public int getStudentId() { return studentId; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getReason() { return reason; }
}