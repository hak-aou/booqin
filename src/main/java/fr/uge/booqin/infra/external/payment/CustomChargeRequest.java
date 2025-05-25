package fr.uge.booqin.infra.external.payment;

import java.util.UUID;

public class CustomChargeRequest {
    private UUID orderId;
    private Long cartVersion;
    private String paymentMethodId;
    private int amount;
    private String currency = "eur";
    private String description;

    public CustomChargeRequest(UUID orderId, UUID cartVersion, String paymentMethodId, int amount) {
        this.orderId = orderId;
        this.paymentMethodId = paymentMethodId;
        this.amount = amount;
    }

    public CustomChargeRequest() {
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID cartId) {
        this.orderId = cartId;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCartVersion() {
        return cartVersion;
    }

    public Long setCartVersion(Long cartVersion) {
        this.cartVersion = cartVersion;
        return cartVersion;
    }
}