package com.example.expensemanagement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    // thêm TextView để hiển thị số dư, thu, chi
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
        adapter = new TransactionAdapter(items);
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
            // Sau khi thêm xong thì reload lại toàn bộ
            loadTransactions();
            updateSummary();
        }
    }
}
