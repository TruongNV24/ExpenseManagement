package com.example.expensemanagement.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.*;

public class FirestoreHelper {

    private FirebaseFirestore db;
    private String userId;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    private CollectionReference getCategoriesCollection() {
        return db.collection("users").document(userId).collection("categories");
    }

    private CollectionReference getTransactionsCollection() {
        return db.collection("users").document(userId).collection("transactions");
    }

    // ---------------- CATEGORY ---------------- //

    public void insertCategoryIfNotExists(String name, String type, FirestoreCallback<String> callback) {
        getCategoriesCollection()
                .whereEqualTo("name", name)
                .whereEqualTo("type", type)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            String existingDocId = task.getResult().getDocuments().get(0).getId();
                            callback.onCallback(existingDocId);
                        } else {
                            Map<String, Object> cat = new HashMap<>();
                            cat.put("name", name);
                            cat.put("type", type);

                            getCategoriesCollection().add(cat)
                                    .addOnSuccessListener(doc -> callback.onCallback(doc.getId()))
                                    .addOnFailureListener(e -> callback.onCallback(null));
                        }
                    } else {
                        callback.onCallback(null);
                    }
                });
    }

    public void getAllCategories(FirestoreListCallback<Category> callback) {
        getCategoriesCollection().get().addOnCompleteListener(task -> {
            List<Category> list = new ArrayList<>();
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String name = doc.getString("name");
                    String type = doc.getString("type");
                    String docId = doc.getId();
                    list.add(new Category(docId, name, type));
                }
            }
            callback.onCallback(list);
        });
    }

    // ---------------- TRANSACTION ---------------- //

    public void insertTransaction(Transaction tx, FirestoreCallback<String> callback) {
        if (getTransactionsCollection() == null) {
            callback.onCallback(null);
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("amount", tx.getAmount());
        map.put("note", tx.getNote());
        map.put("categoryId", tx.getCategoryId());
        map.put("date", tx.getDate()); // YYYY-MM-DD
        map.put("type", tx.isIncome() ? "Income" : "Expense");
        map.put("categoryName", tx.getCategoryName());

        getTransactionsCollection().add(map)
                .addOnSuccessListener(doc -> callback.onCallback(doc.getId()))
                .addOnFailureListener(e -> callback.onCallback(null));
    }

    public void updateTransaction(Transaction tx) {
        if (tx.getDocId() != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("amount", tx.getAmount());
            map.put("note", tx.getNote());
            map.put("categoryId", tx.getCategoryId());
            map.put("date", tx.getDate());
            map.put("type", tx.isIncome() ? "Income" : "Expense");
            map.put("categoryName", tx.getCategoryName());

            getTransactionsCollection().document(tx.getDocId()).set(map);
        }
    }

    public void deleteTransaction(String docId) {
        getTransactionsCollection().document(docId).delete();
    }

    public void getAllTransactions(FirestoreListCallback<Transaction> callback) {
        getTransactionsCollection()
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    List<Transaction> list = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Transaction tx = doc.toObject(Transaction.class);
                            tx.setDocId(doc.getId());
                            String type = doc.getString("type");
                            tx.setIncome("Income".equals(type));
                            if (tx.getCategoryName() == null) tx.setCategoryName("Unknown");
                            list.add(tx);
                        }
                    }
                    callback.onCallback(list);
                });
    }

    // ---------------- FILTERED / MONTHLY TRANSACTIONS ---------------- //

    public void getFilteredTransactions(String fromDate, String toDate, String categoryName, String type,
                                        FirestoreListCallback<Transaction> callback) {

        Query query = getTransactionsCollection();

        if (fromDate != null && !fromDate.isEmpty()) {
            query = query.whereGreaterThanOrEqualTo("date", fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            query = query.whereLessThanOrEqualTo("date", toDate);
        }

        if (categoryName != null && !categoryName.isEmpty() && !categoryName.equals("All")) {
            query = query.whereEqualTo("categoryName", categoryName);
        }

        if (type != null && !type.isEmpty() && !type.equals("All")) {
            query = query.whereEqualTo("type", type);
        }

        query.get().addOnCompleteListener(task -> {
            List<Transaction> list = new ArrayList<>();
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Transaction tx = doc.toObject(Transaction.class);
                    tx.setDocId(doc.getId());
                    String t = doc.getString("type");
                    tx.setIncome("Income".equals(t));
                    if (tx.getCategoryName() == null) tx.setCategoryName("Unknown");
                    list.add(tx);
                }
            }
            callback.onCallback(list);
        });
    }

    public void getTransactionsByMonth(int year, int month, String type, FirestoreListCallback<Transaction> callback) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.YEAR, year);
        start.set(Calendar.MONTH, month);
        start.set(Calendar.DAY_OF_MONTH, 1);
        String fromDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(start.getTime());

        Calendar end = (Calendar) start.clone();
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
        String toDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(end.getTime());

        getFilteredTransactions(fromDate, toDate, "", type, callback);
    }

    // ---------------- SUMMARY ---------------- //

    public void getSummary(FirestoreMapCallback callback) {
        getTransactionsCollection().whereEqualTo("type", "Income").get()
                .addOnSuccessListener(snapIncome -> {
                    double totalIncome = snapIncome.getDocuments().stream()
                            .map(doc -> doc.getDouble("amount"))
                            .filter(Objects::nonNull)
                            .mapToDouble(Double::doubleValue)
                            .sum();

                    getTransactionsCollection().whereEqualTo("type", "Expense").get()
                            .addOnSuccessListener(snapExpense -> {
                                double totalExpense = snapExpense.getDocuments().stream()
                                        .map(doc -> doc.getDouble("amount"))
                                        .filter(Objects::nonNull)
                                        .mapToDouble(Double::doubleValue)
                                        .sum();

                                Map<String, Double> summary = new HashMap<>();
                                summary.put("income", totalIncome);
                                summary.put("expense", totalExpense);
                                summary.put("balance", totalIncome - totalExpense);
                                callback.onCallback(summary);
                            })
                            .addOnFailureListener(e -> {
                                Map<String, Double> summary = new HashMap<>();
                                summary.put("income", totalIncome);
                                summary.put("expense", 0.0);
                                summary.put("balance", totalIncome);
                                callback.onCallback(summary);
                            });
                })
                .addOnFailureListener(e -> {
                    Map<String, Double> summary = new HashMap<>();
                    summary.put("income", 0.0);
                    summary.put("expense", 0.0);
                    summary.put("balance", 0.0);
                    callback.onCallback(summary);
                });
    }

    public CollectionReference getTransactionsCollectionRef() {
        return getTransactionsCollection();
    }

    // ---------------- CALLBACK INTERFACES ---------------- //

    public interface FirestoreCallback<T> {
        void onCallback(T data);
    }

    public interface FirestoreListCallback<T> {
        void onCallback(List<T> list);
    }

    public interface FirestoreMapCallback {
        void onCallback(Map<String, Double> map);
    }
}
