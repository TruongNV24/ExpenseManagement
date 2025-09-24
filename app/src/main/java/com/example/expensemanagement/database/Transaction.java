package com.example.expensemanagement.database;

import java.io.Serializable;

public class Transaction implements Serializable {
    private String docId; // docId từ Firestore
    private double amount;
    private String note;
    private String date;
    private boolean isIncome;
    private String type; // "Income" hoặc "Expense"
    private String categoryId; // docId của category
    private String categoryName;

    public Transaction() {}

    public Transaction(String note, String date, double amount, boolean isIncome) {
        this.note = note;
        this.date = date;
        this.amount = amount;
        this.isIncome = isIncome;
        this.type = isIncome ? "Income" : "Expense";
    }

    public Transaction(String docId, String note, String date, double amount, boolean isIncome, String categoryId, String categoryName) {
        this.docId = docId;
        this.note = note;
        this.date = date;
        this.amount = amount;
        this.isIncome = isIncome;
        this.type = isIncome ? "Income" : "Expense";
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isIncome() { return isIncome; }
    public void setIncome(boolean income) {
        isIncome = income;
        this.type = income ? "Income" : "Expense";
    }

    public String getType() { return type; }
    public void setType(String type) {
        this.type = type;
        this.isIncome = "Income".equals(type);
    }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
