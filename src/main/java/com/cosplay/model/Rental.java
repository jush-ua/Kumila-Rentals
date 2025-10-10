package com.cosplay.model;

import java.time.LocalDate;

public class Rental {
    private int id;
    private int costumeId;
    private String customerName;
    private String contactNumber;
    private String address;
    private String facebookLink;
    private LocalDate startDate;
    private LocalDate endDate;
    private String paymentMethod;
    private String proofOfPayment;
    private String status;

    public Rental() {}

    // getters / setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCostumeId() { return costumeId; }
    public void setCostumeId(int costumeId) { this.costumeId = costumeId; }
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Rental[%d] Costume:%d Name:%s %s->%s Status:%s",
                id, costumeId, customerName, startDate, endDate, status);
    }
}
