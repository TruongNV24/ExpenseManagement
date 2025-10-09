package com.example.expensemanagement;

import android.app.AlertDialog;
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
                showEditDialog(transaction);
            }

            @Override
            public void onDelete(Transaction transaction) {
                confirmDelete(transaction, () -> {
                    firestoreHelper.deleteTransaction(transaction.getDocId());
                    Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                });
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
                    if (error != null || value == null) return;

                    List<Transaction> newList = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Transaction tx = doc.toObject(Transaction.class);
                        if (tx != null) {
                            tx.setDocId(doc.getId());
                            newList.add(tx);
                        }
                    }
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

    private void showEditDialog(Transaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_transaction, null);
        builder.setView(dialogView);

        EditText edtNote = dialogView.findViewById(R.id.edtNote);
        EditText edtAmount = dialogView.findViewById(R.id.edtAmount);
        EditText edtCategory = dialogView.findViewById(R.id.edtCategory);
        Spinner spinnerType = dialogView.findViewById(R.id.spinnerType);
        EditText edtDate = dialogView.findViewById(R.id.edtDate);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        edtNote.setText(transaction.getNote());
        edtAmount.setText(String.valueOf(transaction.getAmount()));
        edtCategory.setText(transaction.getCategoryName());
        edtDate.setText(transaction.getDate());

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Income", "Expense"}
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        spinnerType.setSelection(transaction.isIncome() ? 0 : 1);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            try {
                transaction.setNote(edtNote.getText().toString());
                transaction.setAmount(Double.parseDouble(edtAmount.getText().toString()));
                transaction.setCategoryName(edtCategory.getText().toString());
                transaction.setDate(edtDate.getText().toString());
                transaction.setIncome(spinnerType.getSelectedItem().toString().equals("Income"));

                firestoreHelper.updateTransaction(transaction);
                Toast.makeText(requireContext(), "Transaction updated", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Invalid data", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            confirmDelete(transaction, () -> {
                firestoreHelper.deleteTransaction(transaction.getDocId());
                Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
    }

    private void confirmDelete(Transaction transaction, Runnable onDeleted) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (d, which) -> onDeleted.run())
                .setNegativeButton("Cancel", null)
                .show();
    }
}
