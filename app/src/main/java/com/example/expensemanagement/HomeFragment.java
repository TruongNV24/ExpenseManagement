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

import com.example.expensemanagement.database.DatabaseHelper;
import com.example.expensemanagement.database.Transaction;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> items;
    private Button btnExpense, btnIncome;
    private DatabaseHelper dbHelper;

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

        dbHelper = DatabaseHelper.getInstance(requireContext());

        items = new ArrayList<>();

        adapter = new TransactionAdapter(items, new TransactionAdapter.OnTransactionActionListener() {
            @Override
            public void onEdit(Transaction transaction) {
                showEditDialog(transaction);
            }

            @Override
            public void onDelete(Transaction transaction) {
                confirmDelete(transaction, () -> {
                    dbHelper.deleteTransaction(transaction.getId());
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
        items.addAll(dbHelper.getAllTransactions());
        adapter.notifyDataSetChanged();
    }

    private void updateSummary() {
        double totalIncome = 0;
        double totalExpense = 0;

        List<Transaction> all = dbHelper.getAllTransactions();
        for (Transaction t : all) {
            if (t.isIncome()) {
                totalIncome += t.getAmount();
            } else {
                totalExpense += t.getAmount();
            }
        }

        double balance = totalIncome - totalExpense;

        tvBalance.setText(String.format("$%,.2f", balance));
        tvIncome.setText(String.format("+$%,.2f", totalIncome));
        tvSpending.setText(String.format("-$%,.2f", totalExpense));
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
                .setMessage("\n" +
                        "Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (d, which) -> {
                    onDeleted.run();
                })
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

            dbHelper.updateTransaction(transaction);
            loadTransactions();
            updateSummary();
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            confirmDelete(transaction, () -> {
                dbHelper.deleteTransaction(transaction.getId());
                loadTransactions();
                updateSummary();
                dialog.dismiss();
            });
        });

        dialog.show();
    }
}
