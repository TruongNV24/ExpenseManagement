package com.example.expensemanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanagement.database.FirestoreHelper;
import com.example.expensemanagement.database.Transaction;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TransactionsFragment extends Fragment {

    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> items;

    private EditText edtFromDate, edtToDate;
    private Spinner spinnerCategory, spinnerType;
    private Button btnFilter;

    private FirestoreHelper firestoreHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transactions, container, false);

        rvTransactions = v.findViewById(R.id.rvTransactions);
        edtFromDate = v.findViewById(R.id.edtFromDate);
        edtToDate = v.findViewById(R.id.edtToDate);
        spinnerCategory = v.findViewById(R.id.spinnerCategory);
        spinnerType = v.findViewById(R.id.spinnerType);
        btnFilter = v.findViewById(R.id.btnFilter);

        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));

        firestoreHelper = new FirestoreHelper();

        items = new ArrayList<>();
        adapter = new TransactionAdapter(items, new TransactionAdapter.OnTransactionActionListener() {
            @Override
            public void onEdit(Transaction transaction) {
                // Gọi dialog edit
            }

            @Override
            public void onDelete(Transaction transaction) {
                firestoreHelper.deleteTransaction(transaction.getDocId());
            }
        });
        rvTransactions.setAdapter(adapter);

        setupSpinners();
        setupDatePickersAndFilter();
        setupRealtimeListener(); // <-- snapshot listener

        return v;
    }

    private void setupSpinners() {
        firestoreHelper.getAllCategories(list -> {
            List<String> categories = new ArrayList<>();
            categories.add("All");
            for (com.example.expensemanagement.database.Category c : list) {
                categories.add(c.getName());
            }

            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categories
            );
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(categoryAdapter);
        });

        List<String> types = new ArrayList<>();
        types.add("All");
        types.add("Income");
        types.add("Expense");

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                types
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
    }

    private void setupDatePickersAndFilter() {
        edtFromDate.setOnClickListener(v1 -> showDatePickerDialog(edtFromDate));
        edtToDate.setOnClickListener(v2 -> showDatePickerDialog(edtToDate));
        btnFilter.setOnClickListener(v -> applyFilters());
    }

    private void setupRealtimeListener() {
        firestoreHelper.getTransactionsCollectionRef()
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    List<Transaction> newList = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Transaction tx = doc.toObject(Transaction.class);
                            if (tx != null) {
                                tx.setDocId(doc.getId());
                                newList.add(tx);
                            }
                        }
                    }
                    // Áp dụng filter trên danh sách mới
                    applyRealtimeFilter(newList);
                });
    }

    private void applyRealtimeFilter(List<Transaction> newList) {
        String fromDate = edtFromDate.getText().toString().trim();
        String toDate = edtToDate.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "";
        String type = spinnerType.getSelectedItem() != null ? spinnerType.getSelectedItem().toString() : "";

        items.clear();
        for (Transaction tx : newList) {
            boolean matches = true;
            if (!fromDate.isEmpty() && tx.getDate().compareTo(fromDate) < 0) matches = false;
            if (!toDate.isEmpty() && tx.getDate().compareTo(toDate) > 0) matches = false;
            if (!category.equals("All") && !tx.getCategoryName().equals(category)) matches = false;
            if (!type.equals("All") && (type.equals("Income") != tx.isIncome())) matches = false;

            if (matches) items.add(tx);
        }
        adapter.notifyDataSetChanged();
    }

    private void applyFilters() {
        // Lấy snapshot mới và áp dụng filter
        firestoreHelper.getTransactionsCollectionRef()
                .get()
                .addOnSuccessListener(value -> {
                    List<Transaction> newList = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Transaction tx = doc.toObject(Transaction.class);
                        if (tx != null) tx.setDocId(doc.getId());
                        newList.add(tx);
                    }
                    applyRealtimeFilter(newList);
                });
    }

    private void showDatePickerDialog(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, year1, monthOfYear, dayOfMonth) -> {
            String date = year1 + "-" + String.format("%02d", (monthOfYear + 1))
                    + "-" + String.format("%02d", dayOfMonth);
            editText.setText(date);
        }, year, month, day).show();
    }
}
