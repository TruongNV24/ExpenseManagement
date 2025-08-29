package com.example.expensemanagement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> items;
    private Button btnExpense, btnIncome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerTransactions = v.findViewById(R.id.recyclerTransactions);
        recyclerTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTransactions.setNestedScrollingEnabled(false);

        // ánh xạ button
        btnExpense = v.findViewById(R.id.btnExpense);
        btnIncome = v.findViewById(R.id.btnIncome);

        // dữ liệu mẫu
        items = new ArrayList<>();
        items.add(new Transaction("Added money",   "Aug 30 2024", 10.00,  true));
        items.add(new Transaction("Withdraw Funds","Jul 14 2024",  8.00,  false));
        items.add(new Transaction("Added money",   "Jul 05 2024", 20.00,  true));

        adapter = new TransactionAdapter(items);
        recyclerTransactions.setAdapter(adapter);

        // sự kiện nhấn nút
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

    // nhận kết quả trả về
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            Transaction transaction = (Transaction) data.getSerializableExtra("transaction");
            if (transaction != null) {
                items.add(0, transaction); // thêm vào đầu danh sách
                adapter.notifyItemInserted(0);
                recyclerTransactions.scrollToPosition(0);
            }
        }
    }
}
