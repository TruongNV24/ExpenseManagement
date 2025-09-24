package com.example.expensemanagement;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanagement.database.Category;
import com.example.expensemanagement.database.CategoryStat;
import com.example.expensemanagement.database.FirestoreHelper;
import com.example.expensemanagement.database.Transaction;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private PieChart pieChart;
    private FirestoreHelper firestore;
    private Button btnExpense, btnIncome;
    private TextView tvMonth, tvDateRange;
    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;

    private TextView tvExpense, tvIncome, tvBalance;

    private Calendar selectedMonth;
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
    private SimpleDateFormat dayMonthFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
    private SimpleDateFormat fireDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final String COLOR_EXPENSE = "#FF4444";
    private static final String COLOR_INCOME  = "#33AA33";
    private static final String COLOR_DISABLED = "#AAAAAA";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        firestore = new FirestoreHelper();
        pieChart = view.findViewById(R.id.pieChart);
        btnExpense = view.findViewById(R.id.btnExpense);
        btnIncome = view.findViewById(R.id.btnIncome);
        tvMonth = view.findViewById(R.id.tvMonth);
        tvDateRange = view.findViewById(R.id.tvDateRange);

        recyclerView = view.findViewById(R.id.recyclerCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        categoryAdapter = new CategoryAdapter();
        recyclerView.setAdapter(categoryAdapter);

        tvExpense = view.findViewById(R.id.tvExpense);
        tvIncome = view.findViewById(R.id.tvIncome);
        tvBalance = view.findViewById(R.id.tvBalance);

        selectedMonth = Calendar.getInstance();
        updateMonthLabels();

        setActiveTab(true);
        loadMonthData(true);
        updateSummary();

        btnExpense.setOnClickListener(v -> {
            setActiveTab(true);
            loadMonthData(true);
            updateSummary();
        });

        btnIncome.setOnClickListener(v -> {
            setActiveTab(false);
            loadMonthData(false);
            updateSummary();
        });

        tvMonth.setOnClickListener(v -> showMonthPicker());

        return view;
    }

    private void showMonthPicker() {
        int year = selectedMonth.get(Calendar.YEAR);
        int month = selectedMonth.get(Calendar.MONTH);

        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (DatePicker view, int y, int m, int dayOfMonth) -> {
                    selectedMonth.set(Calendar.YEAR, y);
                    selectedMonth.set(Calendar.MONTH, m);
                    selectedMonth.set(Calendar.DAY_OF_MONTH, 1);

                    updateMonthLabels();

                    if (btnExpense.getCurrentTextColor() == Color.WHITE) {
                        loadMonthData(true);
                    } else {
                        loadMonthData(false);
                    }
                    updateSummary();
                }, year, month, 1);

        dialog.show();
    }

    private void updateMonthLabels() {
        tvMonth.setText(monthFormat.format(selectedMonth.getTime()));

        Calendar start = (Calendar) selectedMonth.clone();
        start.set(Calendar.DAY_OF_MONTH, 1);

        Calendar end = (Calendar) selectedMonth.clone();
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));

        tvDateRange.setText(dayMonthFormat.format(start.getTime()) + " - " +
                dayMonthFormat.format(end.getTime()));
    }

    private void setActiveTab(boolean isExpense) {
        if (isExpense) {
            btnExpense.setBackgroundColor(Color.parseColor(COLOR_EXPENSE));
            btnExpense.setTextColor(Color.WHITE);

            btnIncome.setBackgroundColor(Color.parseColor(COLOR_DISABLED));
            btnIncome.setTextColor(Color.BLACK);
        } else {
            btnIncome.setBackgroundColor(Color.parseColor(COLOR_INCOME));
            btnIncome.setTextColor(Color.WHITE);

            btnExpense.setBackgroundColor(Color.parseColor(COLOR_DISABLED));
            btnExpense.setTextColor(Color.BLACK);
        }
    }

    private void updateSummary() {
        Calendar start = (Calendar) selectedMonth.clone();
        start.set(Calendar.DAY_OF_MONTH, 1);

        Calendar end = (Calendar) selectedMonth.clone();
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));

        String startDate = fireDateFormat.format(start.getTime());
        String endDate = fireDateFormat.format(end.getTime());

        firestore.getFilteredTransactions(startDate, endDate, "", "All", transactions -> {
            Log.d("Statistics", "Transactions count: " + transactions.size());
            double totalIncome = 0;
            double totalExpense = 0;
            for (Transaction t : transactions) {
                Log.d("Statistics", "Transaction: " + t.getCategoryName() + " - " + t.getAmount());
                if (t.isIncome()) totalIncome += t.getAmount();
                else totalExpense += t.getAmount();
            }
            double balance = totalIncome - totalExpense;

            tvIncome.setText(String.format("+$%,.2f", totalIncome));
            tvExpense.setText(String.format("-$%,.2f", totalExpense));
            tvBalance.setText(String.format("$%,.2f", balance));
        });
    }

    private void loadMonthData(boolean isExpense) {
        Calendar start = (Calendar) selectedMonth.clone();
        start.set(Calendar.DAY_OF_MONTH, 1);

        Calendar end = (Calendar) selectedMonth.clone();
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));

        String startDate = fireDateFormat.format(start.getTime());
        String endDate   = fireDateFormat.format(end.getTime());
        String type = isExpense ? "Expense" : "Income";

        firestore.getFilteredTransactions(startDate, endDate, "", type, transactions -> {

            Log.d("Statistics", "Transactions for PieChart: " + transactions.size());

            // Tổng tiền theo category
            Map<String, Double> categoryMap = new java.util.HashMap<>();
            for (Transaction t : transactions) {
                String cat = t.getCategoryName() != null ? t.getCategoryName() : "Unknown";
                double amount = t.getAmount();
                if (amount > 0) {
                    categoryMap.put(cat, categoryMap.getOrDefault(cat, 0.0) + amount);
                }
            }

            if (categoryMap.isEmpty()) {
                pieChart.clear();
                pieChart.setNoDataText("No transactions");
                return;
            }

            // PieChart & RecyclerView
            List<CategoryStat> listItems = new ArrayList<>();
            ArrayList<PieEntry> entries = new ArrayList<>();
            float total = 0f;

            for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
                float value = entry.getValue().floatValue();
                total += value;
                entries.add(new PieEntry(value, entry.getKey()));

                Category category = new Category(null, entry.getKey(), type);
                listItems.add(new CategoryStat(category, value));
            }

            categoryAdapter.submitList(listItems);

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(new int[]{
                    Color.rgb(244, 67, 54),
                    Color.rgb(33, 150, 243),
                    Color.rgb(76, 175, 80),
                    Color.rgb(255, 193, 7),
                    Color.rgb(156, 39, 176),
                    Color.rgb(0, 188, 212),
                    Color.rgb(255, 87, 34)
            });
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(12f);

            PieData pieData = new PieData(dataSet);
            pieChart.setData(pieData);
            pieChart.setUsePercentValues(true);
            pieChart.getDescription().setEnabled(false);
            pieChart.setDrawEntryLabels(false);
            pieChart.setHoleRadius(45f);
            pieChart.setTransparentCircleRadius(50f);

            pieChart.setCenterText((isExpense ? "Total Expense:\n" : "Total Income:\n") + String.format("$%,.2f", total));
            pieChart.setCenterTextSize(14f);
            pieChart.setCenterTextColor(Color.BLACK);

            Legend legend = pieChart.getLegend();
            legend.setEnabled(true);
            legend.setTextSize(12f);
            legend.setForm(Legend.LegendForm.CIRCLE);

            pieChart.animateY(800);
            pieChart.invalidate();
        });
    }
}
