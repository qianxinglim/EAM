package com.example.eam.model;

public class Attendance {
    private String userId;
    private String clockInDate;
    private String clockInTime;
    private long clockInTimestamp;
    private String clockOutDate;
    private String clockOutTime;
    private long clockOutTimestamp;
    private int duration;
    //private long oriClockOutTimestamp;
    private double clockInLat;
    private double clockInLong;
    private double clockOutLat;
    private double clockOutLong;
    private boolean clockIninRange;
    private boolean clockOutInRange;
    private String oriClockInTime;
    private String oriClockOutTime;
    private String mustClockOutTime;

    public Attendance() {
    }

    public Attendance(String userId, String clockInDate, String clockInTime, long clockInTimestamp, String clockOutDate, String clockOutTime, long clockOutTimestamp, int duration, long oriClockOutTimestamp, double clockInLat, double clockInLong, double clockOutLat, double clockOutLong, boolean clockIninRange, boolean clockOutInRange, String oriClockInTime, String oriClockOutTime, String mustClockOutTime) {
        this.userId = userId;
        this.clockInDate = clockInDate;
        this.clockInTime = clockInTime;
        this.clockInTimestamp = clockInTimestamp;
        this.clockOutDate = clockOutDate;
        this.clockOutTime = clockOutTime;
        this.clockOutTimestamp = clockOutTimestamp;
        this.duration = duration;
        //this.oriClockOutTimestamp = oriClockOutTimestamp;
        this.clockInLat = clockInLat;
        this.clockInLong = clockInLong;
        this.clockOutLat = clockOutLat;
        this.clockOutLong = clockOutLong;
        this.clockIninRange = clockIninRange;
        this.clockOutInRange = clockOutInRange;
        this.oriClockInTime = oriClockInTime;
        this.oriClockOutTime = oriClockOutTime;
        this.mustClockOutTime = mustClockOutTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public long getClockInTimestamp() {
        return clockInTimestamp;
    }

    public void setClockInTimestamp(long clockInTimestamp) {
        this.clockInTimestamp = clockInTimestamp;
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

    public long getClockOutTimestamp() {
        return clockOutTimestamp;
    }

    public void setClockOutTimestamp(long clockOutTimestamp) {
        this.clockOutTimestamp = clockOutTimestamp;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    /*public long getOriClockOutTimestamp() {
        return oriClockOutTimestamp;
    }

    public void setOriClockOutTimestamp(long oriClockOutTimestamp) {
        this.oriClockOutTimestamp = oriClockOutTimestamp;
    }*/

    public double getClockInLat() {
        return clockInLat;
    }

    public void setClockInLat(double clockInLat) {
        this.clockInLat = clockInLat;
    }

    public double getClockInLong() {
        return clockInLong;
    }

    public void setClockInLong(double clockInLong) {
        this.clockInLong = clockInLong;
    }

    public double getClockOutLat() {
        return clockOutLat;
    }

    public void setClockOutLat(double clockOutLat) {
        this.clockOutLat = clockOutLat;
    }

    public double getClockOutLong() {
        return clockOutLong;
    }

    public void setClockOutLong(double clockOutLong) {
        this.clockOutLong = clockOutLong;
    }

    public boolean isClockIninRange() {
        return clockIninRange;
    }

    public void setClockIninRange(boolean clockIninRange) {
        this.clockIninRange = clockIninRange;
    }

    public boolean isClockOutInRange() {
        return clockOutInRange;
    }

    public void setClockOutInRange(boolean clockOutInRange) {
        this.clockOutInRange = clockOutInRange;
    }

    public String getOriClockInTime() {
        return oriClockInTime;
    }

    public void setOriClockInTime(String oriClockInTime) {
        this.oriClockInTime = oriClockInTime;
    }

    public String getOriClockOutTime() {
        return oriClockOutTime;
    }

    public void setOriClockOutTime(String oriClockOutTime) {
        this.oriClockOutTime = oriClockOutTime;
    }

    public String getMustClockOutTime() {
        return mustClockOutTime;
    }

    public void setMustClockOutTime(String mustClockOutTime) {
        this.mustClockOutTime = mustClockOutTime;
    }
}