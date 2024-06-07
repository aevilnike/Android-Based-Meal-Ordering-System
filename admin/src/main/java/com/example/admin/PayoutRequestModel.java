package com.example.admin;

public class PayoutRequestModel {

    private String status,sellerUid,payoutId,sellerName,sellerProfileImage;
    private double amount;
    private long timestamp;

    public PayoutRequestModel() {
    }

    public PayoutRequestModel(double amount, long timestamp, String status, String sellerUid, String payoutId, String sellerName, String sellerProfileImage) {
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
        this.sellerUid = sellerUid;
        this.payoutId = payoutId;
        this.sellerName = sellerName;
        this.sellerProfileImage = sellerProfileImage;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerProfileImage() {
        return sellerProfileImage;
    }

    public void setSellerProfileImage(String sellerProfileImage) {
        this.sellerProfileImage = sellerProfileImage;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSellerUid() {
        return sellerUid;
    }

    public void setSellerUid(String sellerUid) {
        this.sellerUid = sellerUid;
    }

    public String getPayoutId() {
        return payoutId;
    }

    public void setPayoutId(String payoutId) {
        this.payoutId = payoutId;
    }
}
