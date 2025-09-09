package com.example.expensemanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanagement.database.DatabaseHelper;
import com.example.expensemanagement.database.Transaction;

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

    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transactions, container, false);

        // Ánh xạ view
        rvTransactions = v.findViewById(R.id.rvTransactions);
        edtFromDate = v.findViewById(R.id.edtFromDate);
        edtToDate = v.findViewById(R.id.edtToDate);
        spinnerCategory = v.findViewById(R.id.spinnerCategory);
        spinnerType = v.findViewById(R.id.spinnerType);
        btnFilter = v.findViewById(R.id.btnFilter);

        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));

        dbHelper = DatabaseHelper.getInstance(requireContext());

        items = new ArrayList<>();
        adapter = new TransactionAdapter(items);
        rvTransactions.setAdapter(adapter);

        // Khởi tạo dữ liệu cho Spinner
        setupSpinners();

        // Load tất cả giao dịch ban đầu
        loadTransactions();

        // Chọn ngày bằng DatePicker
        edtFromDate.setOnClickListener(v1 -> showDatePickerDialog(edtFromDate));
        edtToDate.setOnClickListener(v2 -> showDatePickerDialog(edtToDate));

        // Xử lý khi bấm nút Lọc
        btnFilter.setOnClickListener(v3 -> applyFilters());

        return v;
    }

    private void setupSpinners() {
        // Spinner Category (lấy từ DB)
        List<String> categories = new ArrayList<>();
        categories.add("All"); // ✅ dùng "All" thay vì "Tất cả"
        categories.addAll(dbHelper.getCategoryNames());

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Spinner Type (Income/Expense)
        List<String> types = new ArrayList<>();
        types.add("All");      // ✅ đồng bộ với query
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

    private void loadTransactions() {
        items.clear();
        items.addAll(dbHelper.getAllTransactions());
        adapter.notifyDataSetChanged();
    }

    private void applyFilters() {
        String fromDate = edtFromDate.getText().toString().trim();
        String toDate = edtToDate.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "";
        String type = spinnerType.getSelectedItem() != null ? spinnerType.getSelectedItem().toString() : "";

        items.clear();
        items.addAll(dbHelper.getFilteredTransactions(fromDate, toDate, category, type));
        adapter.notifyDataSetChanged();
    }

    private void showDatePickerDialog(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (DatePicker view, int year1, int monthOfYear, int dayOfMonth) -> {
                    String date = year1 + "-" + String.format("%02d", (monthOfYear + 1))
                            + "-" + String.format("%02d", dayOfMonth);
                    editText.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }
}
