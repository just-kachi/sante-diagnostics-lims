package lims.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TestRequest {

    private int id;
    private int customerId;
    private String customerName;
    private int testTypeId;
    private String testName;
    private String testCategory;
    private BigDecimal price;
    private int tatHours;
    private String resultFormat;
    private String requestStatus;
    private String paymentStatus;
    private String sampleStatus;
    private String resultStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime estimatedReadyAt;

    public TestRequest() {
    }

    public TestRequest(int id, int customerId, String customerName, int testTypeId,
                       String testName, String testCategory, BigDecimal price,
                       int tatHours, String resultFormat, String requestStatus,
                       String paymentStatus, String sampleStatus, String resultStatus,
                       LocalDateTime requestedAt, LocalDateTime estimatedReadyAt) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.testTypeId = testTypeId;
        this.testName = testName;
        this.testCategory = testCategory;
        this.price = price;
        this.tatHours = tatHours;
        this.resultFormat = resultFormat;
        this.requestStatus = requestStatus;
        this.paymentStatus = paymentStatus;
        this.sampleStatus = sampleStatus;
        this.resultStatus = resultStatus;
        this.requestedAt = requestedAt;
        this.estimatedReadyAt = estimatedReadyAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }


    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }


    public int getTestTypeId() {
        return testTypeId;
    }

    public void setTestTypeId(int testTypeId) {
        this.testTypeId = testTypeId;
    }


    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }


    public String getTestCategory() {
        return testCategory;
    }

    public void setTestCategory(String testCategory) {
        this.testCategory = testCategory;
    }


    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }


    public int getTatHours() {
        return tatHours;
    }

    public void setTatHours(int tatHours) {
        this.tatHours = tatHours;
    }


    public String getResultFormat() {
        return resultFormat;
    }

    public void setResultFormat(String resultFormat) {
        this.resultFormat = resultFormat;
    }


    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
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


    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }


    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }


    public LocalDateTime getEstimatedReadyAt() {
        return estimatedReadyAt;
    }

    public void setEstimatedReadyAt(LocalDateTime estimatedReadyAt) {
        this.estimatedReadyAt = estimatedReadyAt;
    }
}