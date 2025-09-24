package com.example.expensemanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanagement.database.FirestoreHelper;
import com.example.expensemanagement.database.Transaction;
import com.example.expensemanagement.R;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private OnTransactionActionListener listener;
    private FirestoreHelper firestoreHelper;

    public interface OnTransactionActionListener {
        void onEdit(Transaction tx);
        void onDelete(Transaction tx);
    }

    public TransactionAdapter(List<Transaction> transactionList, OnTransactionActionListener listener) {
        this.transactionList = transactionList;
        this.listener = listener;
        firestoreHelper = new FirestoreHelper(); // khởi tạo FirestoreHelper
    }

    public void setData(List<Transaction> list) {
        this.transactionList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction tx = transactionList.get(position);
        if (tx == null) return;

        holder.txtNote.setText(tx.getNote());
        holder.txtDate.setText(tx.getDate());
        holder.txtCategory.setText(tx.getCategoryName() != null ? tx.getCategoryName() : "Unknown");

        String amountStr = String.format("%.2f", tx.getAmount());
        if (tx.isIncome()) {
            holder.txtAmount.setText("+ " + amountStr);
            holder.txtAmount.setTextColor(holder.itemView.getResources().getColor(android.R.color.holo_green_dark));
            holder.iconType.setImageResource(R.drawable.ic_income);
        } else {
            holder.txtAmount.setText("- " + amountStr);
            holder.txtAmount.setTextColor(holder.itemView.getResources().getColor(android.R.color.holo_red_dark));
            holder.iconType.setImageResource(R.drawable.ic_spending);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(tx);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(tx);
            } else {
                // fallback: xóa trực tiếp Firestore nếu không có listener
                firestoreHelper.deleteTransaction(tx.getDocId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView txtNote, txtDate, txtAmount, txtCategory;
        ImageView iconType;
        ImageView btnEdit, btnDelete;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNote = itemView.findViewById(R.id.txtNote);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtAmount = itemView.findViewById(R.id.txtAmount);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            iconType = itemView.findViewById(R.id.iconType);

            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
