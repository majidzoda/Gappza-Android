package com.fira.gappza;


/**
 * Created by firdavsiimajidzoda on 11/27/16.
 */

public class Transaction {

    //region Fields
    private String number;
    private String amount;
    private String date;
    private String status;
    //endregion

    // Constructor
    public Transaction(String number, String amount, String date, String status) {
        this.number = number;
        this.amount = amount;
        this .date = date;
        this.status = status;
    }

    //region Setters and Getters
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    //endregion

}
