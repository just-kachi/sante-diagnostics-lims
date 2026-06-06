package lims.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TestType {

    private int id;
    private String name;
    private String category;
    private BigDecimal price;
    private int tatHours;
    private String resultFormat;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;

    public TestType() {
    }

    public TestType(int id, String name, String category, BigDecimal price,
                    int tatHours, String resultFormat, String description,
                    boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.tatHours = tatHours;
        this.resultFormat = resultFormat;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
    }

    public TestType(String name, String category, BigDecimal price,
                    int tatHours, String resultFormat, String description) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.tatHours = tatHours;
        this.resultFormat = resultFormat;
        this.description = description;
        this.active = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}