package com.example.expensemanagement;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expensemanagement.database.DatabaseHelper;
import com.example.expensemanagement.database.Transaction;

import java.util.Calendar;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText edtAmount, edtNote, edtDate, edtCategory;
    private RadioGroup radioGroupType;
    private Button btnSave, btnCancel;
    private ImageButton btnBack, btnAdd;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Ánh xạ view
        edtAmount = findViewById(R.id.edtAmount);
        edtNote = findViewById(R.id.edtNote);
        edtDate = findViewById(R.id.edtDate);
        edtCategory = findViewById(R.id.edtCategory);
        radioGroupType = findViewById(R.id.radioGroupType);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
        btnAdd = findViewById(R.id.btnAdd);

        dbHelper = DatabaseHelper.getInstance(this);

        // ✅ Nhận type từ Intent và set RadioButton tương ứng
        String type = getIntent().getStringExtra("type");
        if (type != null) {
            if (type.equals("Income")) {
                radioGroupType.check(R.id.radioIncome);
            } else if (type.equals("Expense")) {
                radioGroupType.check(R.id.radioExpense);
            }
        }

        // Date Picker -> Lưu theo yyyy-MM-dd
        edtDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                String d = year + "-" +
                        String.format("%02d", (month + 1)) + "-" +
                        String.format("%02d", day);
                edtDate.setText(d);  // lưu chuẩn ISO format
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Nút Cancel
        btnCancel.setOnClickListener(v -> finish());

        // Nút Save
        btnSave.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString().trim();
            String note = edtNote.getText().toString().trim();
            String categoryName = edtCategory.getText().toString().trim();
            String date = edtDate.getText().toString().trim();

            if (amountStr.isEmpty() || categoryName.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);

            // Xác định loại giao dịch
            boolean isIncome = (radioGroupType.getCheckedRadioButtonId() == R.id.radioIncome);
            String typeTx = isIncome ? "Income" : "Expense";

            // ✅ Insert Category nếu chưa có
            long categoryId = dbHelper.insertCategoryIfNotExists(categoryName, typeTx);

            // ✅ Tạo Transaction và insert DB
            Transaction transaction = new Transaction(0, note, date, amount, isIncome, (int) categoryId, categoryName);
            long newId = dbHelper.insertTransaction(transaction);

            if (newId > 0) {
                Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK, new Intent()); // báo về HomeFragment/TransactionsFragment load lại
                finish();
            } else {
                Toast.makeText(this, "Error saving transaction", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút Add (optional)
        btnAdd.setOnClickListener(v ->
                Toast.makeText(this, "Add button clicked", Toast.LENGTH_SHORT).show()
        );
    }
}
