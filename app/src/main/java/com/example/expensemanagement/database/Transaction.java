package com.example.expensemanagement.database;

import java.io.Serializable;

public class Transaction implements Serializable {
    private long id;
    private double amount;
    private String note;
    private String date;
    private boolean isIncome;
    private int categoryId;
    private String categoryName;


    public Transaction() {}

    public Transaction(String note, String date, double amount, boolean isIncome) {
        this.note = note;
        this.date = date;
        this.amount = amount;
        this.isIncome = isIncome;
    }

    public Transaction(long id, String note, String date, double amount, boolean isIncome, int categoryId, String categoryName) {
        this.id = id;
        this.note = note;
        this.date = date;
        this.amount = amount;
        this.isIncome = isIncome;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public long getId()
    {
        return id;
    }
    public void setId(long id)
    {
        this.id = id;
    }
    public double getAmount()
    {
        return amount;
    }
    public void setAmount(double amount)
    {
        this.amount = amount;
    }
    public String getNote()
    {
        return note;
    }
    public void setNote(String note)
    {
        this.note = note;
    }
    public String getDate()
    {
        return date;
    }
    public void setDate(String date)
    {
        this.date = date;
    }
    public boolean isIncome()
    {
        return isIncome;
    }
    public void setIncome(boolean income)
    {
        isIncome = income;
    }
    public int getCategoryId()
    {
        return categoryId;
    }
    public void setCategoryId(int categoryId)
    {
        this.categoryId = categoryId;
    }
    public String getCategoryName()
    {
        return categoryName;
    }
    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }
}
