package bg.sofia.uni.fmi.mjt.server.financials;

import bg.sofia.uni.fmi.mjt.server.dto.Currency;

import java.io.Serializable;

public class Transaction implements Serializable {
    private final Status transactionStatus;
    private final String user;
    private final double amount;
    private final Currency asset;
    private final String date;
    private final int transactionId;

    public Transaction(Status transactionStatus,
                       String user,
                       double amount,
                       Currency asset,
                       String date) {

        this.date = date;
        this.asset = asset;
        this.transactionStatus = transactionStatus;
        this.user = user;
        this.amount = amount;
        this.transactionId = calculateTransactionId();
    }

    private int calculateTransactionId() {
        return user.hashCode() + asset.hashCode() + date.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Transaction otherTransaction)) {
            return false;
        }

        if (this.date.equals(otherTransaction.date)) {
            return this.user.equals(otherTransaction.user);
        }

        return this.date.equals(otherTransaction.date);

    }

    @Override
    public int hashCode() {
        return user.hashCode()  + date.hashCode();
    }

    //    + asset.hashCode()

    @Override
    public String toString() {
        return "[status: " + transactionStatus.status +
                ", date: " + date +
                ", user: " + user +
                ", amount: " + amount +
                ", asset: " + asset.assetId() +
                ", " + asset.priceUsd() + "$]";
    }

    public enum Status {
        SELL("sell"),
        BUY("buy");

        private final String status;
        Status(String status) {
            this.status = status;
        }

        String getStatus() {
            return status;
        }
    }

    public Currency getAsset() {
        return asset;
    }

    public double getAmount() {
        return amount;
    }

    public Status getTransactionStatus() {
        return transactionStatus;
    }
}
