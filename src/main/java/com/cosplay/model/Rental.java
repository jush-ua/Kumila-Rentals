package com.cosplay.model;

import java.time.LocalDate;

public class Rental {
    private int id;
    private int cosplayId;
    private String customerName;
    private String contactNumber;
    private String address;
    private String facebookLink;
    private LocalDate startDate;
    private LocalDate endDate;
    private int rentDays;
    private String customerAddOns;
    private String paymentMethod;
    private String proofOfPayment;
    private String selfiePhoto;
    private String idPhoto;
    private String status;

    public Rental() {}

    // getters / setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCosplayId() { return cosplayId; }
    public void setCosplayId(int cosplayId) { this.cosplayId = cosplayId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getFacebookLink() { return facebookLink; }
    public void setFacebookLink(String facebookLink) { this.facebookLink = facebookLink; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getProofOfPayment() { return proofOfPayment; }
    public void setProofOfPayment(String proofOfPayment) { this.proofOfPayment = proofOfPayment; }
    public int getRentDays() { return rentDays; }
    public void setRentDays(int rentDays) { this.rentDays = rentDays; }
    public String getCustomerAddOns() { return customerAddOns; }
    public void setCustomerAddOns(String customerAddOns) { this.customerAddOns = customerAddOns; }
    public String getSelfiePhoto() { return selfiePhoto; }
    public void setSelfiePhoto(String selfiePhoto) { this.selfiePhoto = selfiePhoto; }
    public String getIdPhoto() { return idPhoto; }
    public void setIdPhoto(String idPhoto) { this.idPhoto = idPhoto; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Rental[%d] Cosplay:%d Name:%s %s->%s Status:%s",
                id, cosplayId, customerName, startDate, endDate, status);
    }
}

