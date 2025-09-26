package com.example.expensemanagement;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.expensemanagement.database.Goal;
import com.example.expensemanagement.database.Transaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerTransactions, recyclerGoals;
    private TransactionAdapter transactionAdapter;
    private GoalAdapter goalAdapter;

    private List<Transaction> items;
    private List<Goal> goals;

    private Button btnExpense, btnIncome, btnAddGoal;
    private FirestoreHelper firestore;
    private TextView tvBalance, tvIncome, tvSpending;
    private ImageView ivProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerTransactions = v.findViewById(R.id.recyclerTransactions);
        recyclerTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTransactions.setNestedScrollingEnabled(false);

        recyclerGoals = v.findViewById(R.id.recyclerGoals);
        recyclerGoals.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerGoals.setNestedScrollingEnabled(false);

        btnExpense = v.findViewById(R.id.btnExpense);
        btnIncome = v.findViewById(R.id.btnIncome);
        btnAddGoal = v.findViewById(R.id.btnAddGoal);

        tvBalance = v.findViewById(R.id.tvBalance);
        tvIncome = v.findViewById(R.id.tvIncome);
        tvSpending = v.findViewById(R.id.tvSpending);

        ivProfile = v.findViewById(R.id.ivProfile);

        firestore = new FirestoreHelper();

        items = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(items, new TransactionAdapter.OnTransactionActionListener() {
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
        recyclerTransactions.setAdapter(transactionAdapter);

        goals = new ArrayList<>();
        goalAdapter = new GoalAdapter(goals, goal -> {
            if (goal.getCurrentAmount() >= goal.getTargetAmount()) {
                confirmGoalCompletion(goal);
            } else {
                showGoalDialog(goal);
            }
        });
        recyclerGoals.setAdapter(goalAdapter);

        loadTransactions();
        updateSummary();
        loadGoals();

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

        btnAddGoal.setOnClickListener(view -> showAddGoalDialog());

        ivProfile.setOnClickListener(v1 -> showProfileMenu(v1));

        return v;
    }

    private void showAddGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_goal, null);
        builder.setView(dialogView);

        EditText edtName = dialogView.findViewById(R.id.edtGoalName);
        EditText edtTarget = dialogView.findViewById(R.id.edtGoalTarget);
        Button btnSave = dialogView.findViewById(R.id.btnSaveGoal);
        Button btnDelete = dialogView.findViewById(R.id.btnDeleteGoal);

        btnDelete.setVisibility(View.GONE);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String targetStr = edtTarget.getText().toString().trim();
            if (name.isEmpty() || targetStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            double target = Double.parseDouble(targetStr);

            Goal newGoal = new Goal(name, target);
            firestore.insertGoal(newGoal, id -> {
                loadGoals();
                dialog.dismiss();
            });
        });
    }

    private void loadGoals() {
        goals.clear();
        firestore.getSummary(summary -> {
            double balance = summary.getOrDefault("balance", 0.0);

            firestore.getAllGoals(goalList -> {
                for (Goal g : goalList) {
                    g.setCurrentAmount(balance);
                }
                goals.addAll(goalList);
                goalAdapter.notifyDataSetChanged();
            });
        });
    }


    private void loadTransactions() {
        items.clear();
        firestore.getAllTransactions(transactions -> {
            items.addAll(transactions);
            transactionAdapter.notifyDataSetChanged();
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
                .setMessage("Are you sure you want to delete this transaction?")
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
        dialog.show();

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
    }

    private void showGoalDialog(Goal goal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_goal, null);
        builder.setView(dialogView);

        EditText edtName = dialogView.findViewById(R.id.edtGoalName);
        EditText edtTarget = dialogView.findViewById(R.id.edtGoalTarget);
        Button btnSave = dialogView.findViewById(R.id.btnSaveGoal);
        Button btnDelete = dialogView.findViewById(R.id.btnDeleteGoal);

        edtName.setText(goal.getName());
        edtTarget.setText(String.valueOf(goal.getTargetAmount()));

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            goal.setName(edtName.getText().toString());
            goal.setTargetAmount(Double.parseDouble(edtTarget.getText().toString()));

            firestore.updateGoal(goal);
            loadGoals();
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirm")
                    .setMessage("Delete this goal?")
                    .setPositiveButton("Delete", (d, which) -> {
                        firestore.deleteGoal(goal.getDocId());
                        loadGoals();
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void confirmGoalCompletion(Goal goal) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Complete the goal")
                .setMessage("You have reached your goal. \"" + goal.getName() + "\".\nDo you want to complete this goal?")
                .setPositiveButton("Yes", (d, which) -> {
                    Transaction t = new Transaction();
                    t.setAmount(goal.getTargetAmount());
                    t.setNote(goal.getName());
                    t.setCategoryName("Goal");
                    t.setIncome(false);
                    t.setDate(LocalDate.now().toString());

                    firestore.insertTransaction(t, id -> {
                        firestore.deleteGoal(goal.getDocId());
                        loadGoals();
                        loadTransactions();
                        updateSummary();
                        Toast.makeText(requireContext(), "Goal accomplished!", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showProfileMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_logout) {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut();

                SharedPreferences prefs = requireContext()
                        .getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);
                prefs.edit().clear().apply();

                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
                return true;
            }
            return false;
        });

        popup.show();
    }
}
