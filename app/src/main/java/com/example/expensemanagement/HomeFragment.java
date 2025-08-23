package com.example.expensemanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanagement.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerTransactions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerTransactions = v.findViewById(R.id.recyclerTransactions);
        recyclerTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTransactions.setNestedScrollingEnabled(false);

        List<Transaction> items = new ArrayList<>();
        items.add(new Transaction("Added money",   "Aug 30 2024", 10.00,  true));
        items.add(new Transaction("Withdraw Funds","Jul 14 2024",  8.00,  false));
        items.add(new Transaction("Added money",   "Jul 05 2024", 20.00,  true));

        recyclerTransactions.setAdapter(new TransactionAdapter(items));
        return v;
    }
}

