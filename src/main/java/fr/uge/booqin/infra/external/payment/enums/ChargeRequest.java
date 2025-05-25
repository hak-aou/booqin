package fr.uge.booqin.infra.external.payment.enums;

public class ChargeRequest {

    public enum Currency {
        EUR("EUR"),
        USD("USD");

        Currency(String nameCurrency) {}
    }

    private String description;
    private int amount;
    private Currency currency;
    private String stripeEmail;
    private String stripeToken;
    // Default constructor
    public ChargeRequest() {
    }

    // Constructor with all fields
    public ChargeRequest(String description, int amount, Currency currency, String stripeEmail, String stripeToken) {
        this.description = description;
        this.amount = amount;
        this.currency = currency;
        this.stripeEmail = stripeEmail;
        this.stripeToken = stripeToken;
    }

    // Getters and Setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getStripeEmail() {
        return stripeEmail;
    }

    public void setStripeEmail(String stripeEmail) {
        this.stripeEmail = stripeEmail;
    }

    public String getStripeToken() {
        return stripeToken;
    }

    public void setStripeToken(String stripeToken) {
        this.stripeToken = stripeToken;
    }
}