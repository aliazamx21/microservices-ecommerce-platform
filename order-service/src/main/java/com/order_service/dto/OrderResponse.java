package com.order_service.dto;

import java.math.BigDecimal;

public class OrderResponse {

    private String orderId;
    private BigDecimal totalAmount;
    private String status;

    public OrderResponse(String orderId, BigDecimal totalAmount, String status) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}