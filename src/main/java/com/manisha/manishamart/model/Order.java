package com.manisha.manishamart.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private long id;
    private long buyerId;
    private Status status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    public enum Status { PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED }

    public Order() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getBuyerId() { return buyerId; }
    public void setBuyerId(long buyerId) { this.buyerId = buyerId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
