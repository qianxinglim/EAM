package com.example.eam.model;

public class Companies {
    private String companyID;
    private String companyName;
    private String industryType;
    private int staffSize;
    private String creatorID;
    //private String companyLocation;

    public Companies() {
    }

    public Companies(String companyID, String companyName, String industryType, int staffSize, String creatorID) {
        this.companyID = companyID;
        this.companyName = companyName;
        this.industryType = industryType;
        this.staffSize = staffSize;
        this.creatorID = creatorID;
        //this.companyLocation = companyLocation;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public int getStaffSize() {
        return staffSize;
    }

    public void setStaffSize(int staffSize) {
        this.staffSize = staffSize;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    /*public String getCompanyLocation() {
        return companyLocation;
    }

    public void setCompanyLocation(String companyLocation) {
        this.companyLocation = companyLocation;
    }*/
}
