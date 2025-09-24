package com.example.expensemanagement.database;

public class Category {
    private String docId; // docId từ Firestore
    private String name;
    private String type;

    public Category() {}

    public Category(String docId, String name, String type) {
        this.docId = docId;
        this.name = name;
        this.type = type;
    }

    public Category(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
