package com.example.expensemanagement;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText edtAmount, edtNote, edtDate;
    private Spinner spinnerCategory;
    private Button btnSave, btnCancel;

    private String type; // Expense hoặc Income

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        edtAmount = findViewById(R.id.edtAmount);
        edtNote = findViewById(R.id.edtNote);
        edtDate = findViewById(R.id.edtDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Nhận type từ HomeFragment
        type = getIntent().getStringExtra("type");

        // Set spinner category (tạm danh mục mẫu)
        String[] categories = {"Food", "Salary", "Shopping", "Transport"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapter);

        // Date picker
        edtDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                String d = day + "/" + (month + 1) + "/" + year;
                edtDate.setText(d);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString().trim();
            String note = edtNote.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            String date = edtDate.getText().toString().trim();

            if (amountStr.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);

            // Tạo transaction object
            Transaction transaction = new Transaction(note, date, amount, type.equals("Income"));

            // Trả về HomeFragment
            Intent result = new Intent();
            result.putExtra("transaction", transaction);
            setResult(RESULT_OK, result);

            finish();
        });
    }
}
