package com.example.eam.model;

import java.io.Serializable;
import java.util.Map;

public class Company implements Serializable {
    private String companyID;
    private String companyName;
    private String industryType;
    private String staffSize;
    private String creatorID;
    //private String companyLocation;
    private Map<String, Object> companyLocation;
    private double punchRange;

    public Company() {
    }

    public Company(String companyID, String companyName, String industryType, String staffSize, String creatorID, Map<String, Object> companyLocation, double punchRange) {
        this.companyID = companyID;
        this.companyName = companyName;
        this.industryType = industryType;
        this.staffSize = staffSize;
        this.creatorID = creatorID;
        this.companyLocation = companyLocation;
        this.punchRange = punchRange;
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

    public String getStaffSize() {
        return staffSize;
    }

    public void setStaffSize(String staffSize) {
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

    public Map<String, Object> getCompanyLocation() {
        return companyLocation;
    }

    public void setCompanyLocation(Map<String, Object> companyLocation) {
        this.companyLocation = companyLocation;
    }

    public double getPunchRange() {
        return punchRange;
    }

    public void setPunchRange(double punchRange) {
        this.punchRange = punchRange;
    }
}
