package com.example.expensemanagement.database;

public class CategoryStat {
    private Category category;
    private double amount;

    public CategoryStat(Category category, double amount) {
        this.category = category;
        this.amount = amount;
    }

    public Category getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }
}
