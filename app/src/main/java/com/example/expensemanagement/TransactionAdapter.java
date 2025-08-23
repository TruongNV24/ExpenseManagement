package com.example.expensemanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.expensemanagement.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.VH> {

    private final List<Transaction> data;

    public TransactionAdapter(List<Transaction> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Transaction t = data.get(position);
        h.tvTitle.setText(t.getTitle());
        h.tvDate.setText(t.getDate());

        String amountText = String.format(Locale.US, "%s$%.2f",
                t.isIncome() ? "+" : "-", t.getAmount());
        h.tvAmount.setText(amountText);

        if (t.isIncome()) {
            h.tvAmount.setTextColor(h.itemView.getResources().getColor(android.R.color.holo_green_dark));
            h.imgType.setImageResource(R.drawable.ic_income);   // đã có trong project của bạn
        } else {
            h.tvAmount.setTextColor(h.itemView.getResources().getColor(android.R.color.holo_red_dark));
            h.imgType.setImageResource(R.drawable.ic_spending); // đã có trong project của bạn
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgType;
        TextView tvTitle, tvDate, tvAmount;

        VH(@NonNull View itemView) {
            super(itemView);
            imgType = itemView.findViewById(R.id.imgType);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}

