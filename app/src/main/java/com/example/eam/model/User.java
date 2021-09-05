package com.example.eam.model;

import com.bumptech.glide.Glide;
import com.example.eam.EditProfileActivity;
import com.example.eam.R;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.time.Clock;

public class User {
    private String ID;
    private String Name;
    private String PhoneNo;
    private String ProfilePic;
    private String Email;
    private String Status;
    private String Title;
    private String Department;
    private String ClockInTime;
    private String ClockOutTime;
    private int MinutesOfWork;

    public User() {
    }

    public User(String ID, String Name, String PhoneNo, String ProfilePic, String Email, String Status, String Title, String Department, String ClockInTime, String ClockOutTime, int MinutesOfWork) {
        this.ID = ID;
        this.Name = Name;
        this.PhoneNo = PhoneNo;
        this.ProfilePic = ProfilePic;
        this.Email = Email;
        this.Status = Status;
        this.Title = Title;
        this.Department = Department;
        this.ClockInTime = ClockInTime;
        this.ClockOutTime = ClockOutTime;
        this.MinutesOfWork = MinutesOfWork;

    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getPhoneNo() {
        return PhoneNo;
    }

    public void setPhoneNo(String PhoneNo) {
        this.PhoneNo = PhoneNo;
    }

    public String getProfilePic() {
        return ProfilePic;
    }

    public void setProfilePic(String ProfilePic) {
        this.ProfilePic = ProfilePic;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }

    public String getDepartment() {
        return Department;
    }

    public void setDepartment(String Department) {
        this.Department = Department;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    public String getClockInTime() {
        return ClockInTime;
    }

    public void setClockInTime(String ClockInTime) {
        this.ClockInTime = ClockInTime;
    }

    public String getClockOutTime() {
        return ClockOutTime;
    }

    public void setClockOutTime(String ClockOutTime) {
        this.ClockOutTime = ClockOutTime;
    }

    public int getMinutesOfWork() {
        return MinutesOfWork;
    }

    public void setMinutesOfWork(int MinutesOfWork) {
        this.MinutesOfWork = MinutesOfWork;
    }

    @Override
    public String toString(){
        return Name;
    }
}
