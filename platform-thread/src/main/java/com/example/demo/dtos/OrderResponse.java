package com.example.demo.dtos;

public class OrderResponse {
    private Long orderId;
    private String status;
    private String paymentId;
    private Long processingTimeMs;

    public OrderResponse() {}

    public OrderResponse(Long orderId, String status, String paymentId, Long processingTimeMs) {
        this.orderId = orderId;
        this.status = status;
        this.paymentId = paymentId;
        this.processingTimeMs = processingTimeMs;
    }

    // Getters & Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
}