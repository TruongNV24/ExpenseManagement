package com.example.expensemanagement;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanagement.database.FirestoreHelper;
import com.example.expensemanagement.database.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> items;
    private Button btnExpense, btnIncome;
    private FirestoreHelper firestore;

    private TextView tvBalance, tvIncome, tvSpending;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerTransactions = v.findViewById(R.id.recyclerTransactions);
        recyclerTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTransactions.setNestedScrollingEnabled(false);

        btnExpense = v.findViewById(R.id.btnExpense);
        btnIncome = v.findViewById(R.id.btnIncome);

        tvBalance = v.findViewById(R.id.tvBalance);
        tvIncome = v.findViewById(R.id.tvIncome);
        tvSpending = v.findViewById(R.id.tvSpending);

        firestore = new FirestoreHelper();
        items = new ArrayList<>();

        adapter = new TransactionAdapter(items, new TransactionAdapter.OnTransactionActionListener() {
            @Override
            public void onEdit(Transaction transaction) {
                showEditDialog(transaction);
            }

            @Override
            public void onDelete(Transaction transaction) {
                confirmDelete(transaction, () -> {
                    firestore.deleteTransaction(transaction.getDocId());
                    loadTransactions();
                    updateSummary();
                });
            }
        });

        recyclerTransactions.setAdapter(adapter);

        loadTransactions();
        updateSummary();

        btnExpense.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
            intent.putExtra("type", "Expense");
            startActivityForResult(intent, 100);
        });

        btnIncome.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
            intent.putExtra("type", "Income");
            startActivityForResult(intent, 100);
        });

        return v;
    }

    private void loadTransactions() {
        items.clear();
        firestore.getAllTransactions(transactions -> {
            items.addAll(transactions);
            adapter.notifyDataSetChanged();
        });
    }

    private void updateSummary() {
        firestore.getSummary(summary -> {
            double totalIncome = summary.getOrDefault("income", 0.0);
            double totalExpense = summary.getOrDefault("expense", 0.0);
            double balance = summary.getOrDefault("balance", 0.0);

            tvBalance.setText(String.format("$%,.2f", balance));
            tvIncome.setText(String.format("+$%,.2f", totalIncome));
            tvSpending.setText(String.format("-$%,.2f", totalExpense));
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            loadTransactions();
            updateSummary();
        }
    }

    private void confirmDelete(Transaction transaction, Runnable onDeleted) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage("\nAre you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (d, which) -> onDeleted.run())
                .setNegativeButton("Cancel", null)
                .show();
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

        btnSave.setOnClickListener(v -> {
            transaction.setNote(edtNote.getText().toString());
            transaction.setAmount(Double.parseDouble(edtAmount.getText().toString()));
            transaction.setCategoryName(edtCategory.getText().toString());
            transaction.setDate(edtDate.getText().toString());
            transaction.setIncome(spinnerType.getSelectedItem().toString().equals("Income"));

            firestore.updateTransaction(transaction);
            loadTransactions();
            updateSummary();
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            confirmDelete(transaction, () -> {
                firestore.deleteTransaction(transaction.getDocId());
                loadTransactions();
                updateSummary();
                dialog.dismiss();
            });
        });

        dialog.show();
    }
}
