package com.example.eam.model;

import java.io.Serializable;
import java.util.List;

public class Project implements Serializable {
    private String taskId;
    private String createDateTime;
    private String creator;
    private String dueDate;
    private String dueTime;
    private String note;
    private String startDate;
    private String startTime;
    private String title;
    private String status;
    private String doneBy;
    private List<String> assignTo;
    private List<String> attachments;

    public Project() {

    }

    public Project(String taskId, String createDateTime, String creator, String dueDate, String dueTime, String note, String startDate, String startTime, String title, String status, String doneBy, List<String> assignTo, List<String> attachments) {
        this.taskId = taskId;
        this.createDateTime = createDateTime;
        this.creator = creator;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.note = note;
        this.startDate = startDate;
        this.startTime = startTime;
        this.title = title;
        this.status = status;
        this.doneBy = doneBy;
        this.assignTo = assignTo;
        this.attachments = attachments;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(String createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDueTime() {
        return dueTime;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDoneBy() {
        return doneBy;
    }

    public void setDoneBy(String doneBy) {
        this.doneBy = doneBy;
    }

    public List<String> getAssignTo() {
        return assignTo;
    }

    public void setAssignTo(List<String> assignTo) {
        this.assignTo = assignTo;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }
}
