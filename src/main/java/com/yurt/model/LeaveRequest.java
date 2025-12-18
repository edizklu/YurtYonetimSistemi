package com.yurt.model;

import com.yurt.pattern.state.ApprovedState;
import com.yurt.pattern.state.LeaveState;
import com.yurt.pattern.state.PendingState;
import com.yurt.pattern.state.RejectedState;

public class LeaveRequest {
    private int id;
    private int studentId;
    private String startDate;
    private String endDate;
    private String reason;
    private LeaveState state;

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

    public String getStatus() {
        return state.getStatusName();
    }

    public int getId() { return id; }
    public int getStudentId() { return studentId; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getReason() { return reason; }
    public LeaveState getState() { return state; }
}