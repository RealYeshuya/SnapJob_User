package com.example.snapjob_user.Model;

public class Transactions {

    public String transId, userName, address, workerName, workerId, userId, workerArrived, transactionStatus, transactionDate, transactionDescription, review, declineReason, transactionFee;
    public float rating;

    public Transactions(){

    }

    public Transactions(String transId, String userName, String address, String workerId, String userId, String workerName, String workerArrived, String transactionStatus, String transactionDate, String transactionDescription, String review, float rating, String declineReason, String transactionFee){
        this.transId = transId;
        this.userName = userName;
        this.address = address;
        this.workerName = workerName;
        this.workerId = workerId;
        this.userId = userId;
        this.workerArrived = workerArrived;
        this.transactionStatus = transactionStatus;
        this.transactionDate = transactionDate;
        this.transactionDescription = transactionDescription;
        this.rating = rating;
        this.review = review;
        this.declineReason = declineReason;
        this.transactionFee = transactionFee;
    }

    public String getTransactionFee() {
        return transactionFee;
    }

    public void setTransactionFee(String transactionFee) {
        this.transactionFee = transactionFee;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getUserName() { return userName; }

    public void setUserName(String userName) { this.userName = userName; }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWorkerArrived() {
        return workerArrived;
    }

    public void setWorkerArrived(String workerArrived) {
        this.workerArrived = workerArrived;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }
}
