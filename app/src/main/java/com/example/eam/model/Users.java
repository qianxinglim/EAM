package com.example.eam.model;

public class Users {
    private String ID;
    private String Name;
    private String PhoneNo;
    private String ProfilePic;
    private String Email;
    private String Status;
    private String Title;
    private String Department;

    public Users() {
    }

    public Users(String ID, String Name, String PhoneNo, String ProfilePic, String Email, String Status, String Title, String Department) {
        this.ID = ID;
        this.Name = Name;
        this.PhoneNo = PhoneNo;
        this.ProfilePic = ProfilePic;
        this.Email = Email;
        this.Status = Status;
        this.Title = Title;
        this.Department = Department;
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

    public void setTitle(String title) {
        Title = title;
    }

    @Override
    public String toString(){
        return Name;
    }
}
