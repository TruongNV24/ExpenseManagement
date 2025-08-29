package com.example.expensemanagement;

import java.io.Serializable;

public class Transaction implements Serializable {
    private final String title;
    private final String date;
    private final double amount;
    private final boolean isIncome;

    public Transaction(String title, String date, double amount, boolean isIncome) {
        this.title = title;
        this.date = date;
        this.amount = amount;
        this.isIncome = isIncome;

    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public double getAmount() { return amount; }
    public boolean isIncome() { return isIncome; }
}

