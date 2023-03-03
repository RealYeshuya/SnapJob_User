package com.example.snapjob_user.Model;

//Model for Worker to fetch and insert data
public class Worker {

    public String fullName, job, status, phoneNum, email, image, verified, experience, minPay, maxPay, workDesc;

    public Worker(){}

    public void changeName(String text){
        fullName = text;
    }

    public Worker(String fullName, String job, String status, String phoneNum, String email, String image, String verified, String experience, String minPay, String maxPay, String workDesc){
        this.fullName = fullName;
        this.job = job;
        this.status = status;
        this.phoneNum = phoneNum;
        this.email = email;
        this.image = image;
        this.verified = verified;
        this.experience = experience;
        this.minPay = minPay;
        this.maxPay = maxPay;
        this.workDesc = workDesc;
    }

    public String getWorkDesc() {
        return workDesc;
    }

    public void setWorkDesc(String workDesc) {
        this.workDesc = workDesc;
    }

    public String getMinPay() {
        return minPay;
    }

    public void setMinPay(String minPay) {
        this.minPay = minPay;
    }

    public String getMaxPay() {
        return maxPay;
    }

    public void setMaxPay(String maxPay) {
        this.maxPay = maxPay;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFullName() {
        return fullName;
    }

    public String setFullName(String fullName) {
        this.fullName = fullName;
        return fullName;
    }

    public String getJob() {
        return job;
    }

    public String setJob(String job) {
        this.job = job;
        return job;
    }

    public String getStatus() {
        return status;
    }

    public String setStatus(String status) {
        this.status = status;
        return status;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

