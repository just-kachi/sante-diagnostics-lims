package lims.models;

import java.time.LocalDateTime;

public class Result {

    private int id;
    private int requestId;
    private String customerName;
    private String testName;
    private String resultFormat;
    private String paymentStatus;
    private String sampleStatus;
    private String resultValue;
    private String resultStatus;
    private String uploadedByName;
    private String validatedByName;
    private LocalDateTime uploadedAt;
    private LocalDateTime validatedAt;

    public Result() {
    }

    public Result(int id, int requestId, String customerName, String testName,
                  String resultFormat, String paymentStatus, String sampleStatus,
                  String resultValue, String resultStatus, String uploadedByName,
                  String validatedByName, LocalDateTime uploadedAt, LocalDateTime validatedAt) {
        this.id = id;
        this.requestId = requestId;
        this.customerName = customerName;
        this.testName = testName;
        this.resultFormat = resultFormat;
        this.paymentStatus = paymentStatus;
        this.sampleStatus = sampleStatus;
        this.resultValue = resultValue;
        this.resultStatus = resultStatus;
        this.uploadedByName = uploadedByName;
        this.validatedByName = validatedByName;
        this.uploadedAt = uploadedAt;
        this.validatedAt = validatedAt;
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


    public String getResultFormat() {
        return resultFormat;
    }

    public void setResultFormat(String resultFormat) {
        this.resultFormat = resultFormat;
    }


    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }


    public String getSampleStatus() {
        return sampleStatus;
    }

    public void setSampleStatus(String sampleStatus) {
        this.sampleStatus = sampleStatus;
    }


    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }


    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }


    public String getUploadedByName() {
        return uploadedByName;
    }

    public void setUploadedByName(String uploadedByName) {
        this.uploadedByName = uploadedByName;
    }


    public String getValidatedByName() {
        return validatedByName;
    }

    public void setValidatedByName(String validatedByName) {
        this.validatedByName = validatedByName;
    }


    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }


    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }
}