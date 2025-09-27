package com.example.expensemanagement.database;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class Goal {
    @DocumentId
    private String docId;
    private String name;
    private double targetAmount;
    private double currentAmount;
    private boolean completed;
    private Timestamp createdAt;

    public Goal() {
    }

    public Goal(String name, double targetAmount) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = 0;
        this.completed = false;
        this.createdAt = Timestamp.now();
    }

    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount;}
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
