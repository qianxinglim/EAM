package com.example.eam.model;

import java.util.List;

public class Leave {
    private String date;
    private String dateFrom;
    private String dateTo;
    private String document;
    private String duration;
    private boolean fullDay;
    private String image;
    private String note;
    private String requester;
    private String status;
    private String timeFrom;
    private String timeTo;
    private String type;
    private String leaveId;
    private String requestDate;
    private String requestTime;
    private List<String> attachments;

    public Leave() {
    }

    public Leave(String date, String dateFrom, String dateTo, String document, String duration, boolean fullDay, String image, String note, String requester, String status, String timeFrom, String timeTo, String type, String requestDate, String requestTime, List<String> attachments) {
        this.date = date;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.document = document;
        this.duration = duration;
        this.fullDay = fullDay;
        this.image = image;
        this.note = note;
        this.requester = requester;
        this.status = status;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
        this.type = type;
        this.requestDate = requestDate;
        this.requestTime = requestTime;
        this.attachments = attachments;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isFullDay() {
        return fullDay;
    }

    public void setFullDay(boolean fullDay) {
        this.fullDay = fullDay;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(String timeFrom) {
        this.timeFrom = timeFrom;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(String timeTo) {
        this.timeTo = timeTo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(String leaveId) {
        this.leaveId = leaveId;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }
}