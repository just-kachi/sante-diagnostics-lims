package lims.models;

import java.time.LocalDateTime;

public class Sample {

    private int id;
    private int requestId;
    private String customerName;
    private String testName;
    private String sampleStatus;
    private String paymentStatus;
    private String resultStatus;
    private String updatedByName;
    private LocalDateTime updatedAt;

    public Sample() {
    }

    public Sample(int id, int requestId, String customerName, String testName,
                  String sampleStatus, String paymentStatus, String resultStatus,
                  String updatedByName, LocalDateTime updatedAt) {
        this.id = id;
        this.requestId = requestId;
        this.customerName = customerName;
        this.testName = testName;
        this.sampleStatus = sampleStatus;
        this.paymentStatus = paymentStatus;
        this.resultStatus = resultStatus;
        this.updatedByName = updatedByName;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }


    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }


    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }


    public String getSampleStatus() {
        return sampleStatus;
    }

    public void setSampleStatus(String sampleStatus) {
        this.sampleStatus = sampleStatus;
    }


    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }


    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }


    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}