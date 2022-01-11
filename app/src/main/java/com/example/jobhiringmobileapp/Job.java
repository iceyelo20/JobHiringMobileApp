package com.example.jobhiringmobileapp;

public class Job {

    public String detail, positiontofill, requirement, salary, status, timestamp, title, workhours, workplace, date, time, jobId;

    public Job() {
    }

    public Job(String detail, String positiontofill, String requirement, String salary, String status, String timestamp, String title, String workhours, String workplace, String date, String time, String jobId) {
        this.detail = detail;
        this.positiontofill = positiontofill;
        this.requirement = requirement;
        this.salary = salary;
        this.status = status;
        this.timestamp = timestamp;
        this.title = title;
        this.workhours = workhours;
        this.workplace = workplace;
        this.date = date;
        this.time = time;
        this.jobId = jobId;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getPositiontofill() {
        return positiontofill;
    }

    public void setPositiontofill(String positiontofill) {
        this.positiontofill = positiontofill;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWorkhours() {
        return workhours;
    }

    public void setWorkhours(String workhours) {
        this.workhours = workhours;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
