package com.example.eam.model;

public class Attendance {
    private String user;
    private String clockInDate;
    private String clockInTime;
    private String clockOutDate;
    private String clockOutTime;
    private String location;

    public Attendance() {
    }

    public Attendance(String user, String clockInDate, String clockInTime, String clockOutDate, String clockOutTime, String location) {
        this.user = user;
        this.clockInDate = clockInDate;
        this.clockInTime = clockInTime;
        this.clockOutDate = clockOutDate;
        this.clockOutTime = clockOutTime;
        this.location = location;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getClockInDate() {
        return clockInDate;
    }

    public void setClockInDate(String clockInDate) {
        this.clockInDate = clockInDate;
    }

    public String getClockInTime() {
        return clockInTime;
    }

    public void setClockInTime(String clockInTime) {
        this.clockInTime = clockInTime;
    }

    public String getClockOutDate() {
        return clockOutDate;
    }

    public void setClockOutDate(String clockOutDate) {
        this.clockOutDate = clockOutDate;
    }

    public String getClockOutTime() {
        return clockOutTime;
    }

    public void setClockOutTime(String clockOutTime) {
        this.clockOutTime = clockOutTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
