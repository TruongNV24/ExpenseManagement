package com.example.expensemanagement.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Lớp quản lý SQLite Database
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "expense_db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String TABLE_CATEGORIES = "categories";

    // Transactions columns
    private static final String T_COL_ID = "id";
    private static final String T_COL_AMOUNT = "amount";
    private static final String T_COL_NOTE = "note";
    private static final String T_COL_CATEGORY_ID = "category_id";
    private static final String T_COL_DATE = "date";
    private static final String T_COL_TYPE = "type"; // Income / Expense

    // Categories columns
    private static final String C_COL_ID = "id";
    private static final String C_COL_NAME = "name";
    private static final String C_COL_TYPE = "type"; // Income / Expense

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Bảng categories
        String createCategories = "CREATE TABLE " + TABLE_CATEGORIES + " ("
                + C_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + C_COL_NAME + " TEXT NOT NULL, "
                + C_COL_TYPE + " TEXT NOT NULL"
                + ");";

        // Bảng transactions
        String createTransactions = "CREATE TABLE " + TABLE_TRANSACTIONS + " ("
                + T_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + T_COL_AMOUNT + " REAL NOT NULL, "
                + T_COL_NOTE + " TEXT, "
                + T_COL_CATEGORY_ID + " INTEGER, "
                + T_COL_DATE + " TEXT NOT NULL, "
                + T_COL_TYPE + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + T_COL_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + C_COL_ID + ")"
                + ");";

        db.execSQL(createCategories);
        db.execSQL(createTransactions);

        // Dữ liệu mặc định
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + "(" + C_COL_NAME + "," + C_COL_TYPE + ") VALUES('Food','Expense')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + "(" + C_COL_NAME + "," + C_COL_TYPE + ") VALUES('Shopping','Expense')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + "(" + C_COL_NAME + "," + C_COL_TYPE + ") VALUES('Salary','Income')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }

    // ---------- Categories ----------
    public long insertCategoryIfNotExists(String name, String type) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_CATEGORIES, new String[]{C_COL_ID},
                C_COL_NAME + "=? AND " + C_COL_TYPE + "=?", new String[]{name, type},
                null, null, null);

        if (c != null && c.moveToFirst()) {
            int id = c.getInt(c.getColumnIndexOrThrow(C_COL_ID));
            c.close();
            return id;
        }
        if (c != null) c.close();

        SQLiteDatabase wdb = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(C_COL_NAME, name);
        cv.put(C_COL_TYPE, type);
        return wdb.insert(TABLE_CATEGORIES, null, cv);
    }

    public List<Category> getAllCategories(String type) {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_CATEGORIES,
                new String[]{C_COL_ID, C_COL_NAME, C_COL_TYPE},
                C_COL_TYPE + "=?", new String[]{type},
                null, null, C_COL_NAME + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow(C_COL_ID));
                String name = c.getString(c.getColumnIndexOrThrow(C_COL_NAME));
                String t = c.getString(c.getColumnIndexOrThrow(C_COL_TYPE));
                list.add(new Category(id, name, t));
            }
            c.close();
        }
        return list;
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_CATEGORIES,
                new String[]{C_COL_ID, C_COL_NAME, C_COL_TYPE},
                null, null, null, null, C_COL_NAME + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow(C_COL_ID));
                String name = c.getString(c.getColumnIndexOrThrow(C_COL_NAME));
                String t = c.getString(c.getColumnIndexOrThrow(C_COL_TYPE));
                list.add(new Category(id, name, t));
            }
            c.close();
        }
        return list;
    }

    public List<String> getCategoryNames() {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_CATEGORIES,
                new String[]{C_COL_NAME},
                null, null, null, null, C_COL_NAME + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                names.add(c.getString(c.getColumnIndexOrThrow(C_COL_NAME)));
            }
            c.close();
        }
        // thêm option "Tất cả" lên đầu
        names.add(0, "Tất cả");
        return names;
    }


    // ---------- Transactions ----------
    public long insertTransaction(Transaction tx) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(T_COL_AMOUNT, tx.getAmount());
        cv.put(T_COL_NOTE, tx.getNote());
        if (tx.getCategoryId() > 0) {
            cv.put(T_COL_CATEGORY_ID, tx.getCategoryId());
        }
        cv.put(T_COL_DATE, tx.getDate());
        cv.put(T_COL_TYPE, tx.isIncome() ? "Income" : "Expense");
        return db.insert(TABLE_TRANSACTIONS, null, cv);
    }

    public List<Transaction> getFilteredTransactions(String fromDate, String toDate, String categoryName, String type) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder query = new StringBuilder(
                "SELECT t.id, t.note, t.date, t.amount, t.type, t.category_id, c.name as categoryName " +
                        "FROM transactions t " +
                        "LEFT JOIN categories c ON t.category_id = c.id " +
                        "WHERE 1=1"
        );
        List<String> args = new ArrayList<>();

        if (!fromDate.isEmpty()) {
            query.append(" AND t.date >= ?");
            args.add(fromDate);
        }
        if (!toDate.isEmpty()) {
            query.append(" AND t.date <= ?");
            args.add(toDate);
        }
        if (!type.isEmpty() && !type.equals("All")) {
            query.append(" AND t.type = ?");
            if (type.equalsIgnoreCase("Thu nhập") || type.equalsIgnoreCase("Income")) {
                args.add("Income");
            } else if (type.equalsIgnoreCase("Chi tiêu") || type.equalsIgnoreCase("Expense")) {
                args.add("Expense");
            }
        }


        Cursor c = db.rawQuery(query.toString(), args.toArray(new String[0]));
        if (c.moveToFirst()) {
            do {
                Transaction t = new Transaction();
                t.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                t.setNote(c.getString(c.getColumnIndexOrThrow("note")));
                t.setDate(c.getString(c.getColumnIndexOrThrow("date")));
                t.setAmount(c.getDouble(c.getColumnIndexOrThrow("amount")));

                // Xác định là thu nhập hay chi tiêu
                String txType = c.getString(c.getColumnIndexOrThrow("type"));
                t.setIncome("Income".equals(txType));

                t.setCategoryId(c.getInt(c.getColumnIndexOrThrow("category_id")));
                t.setCategoryName(c.getString(c.getColumnIndexOrThrow("categoryName")));
                list.add(t);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean updateTransaction(Transaction tx) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(T_COL_AMOUNT, tx.getAmount());
        cv.put(T_COL_NOTE, tx.getNote());
        cv.put(T_COL_CATEGORY_ID, tx.getCategoryId());
        cv.put(T_COL_DATE, tx.getDate());
        cv.put(T_COL_TYPE, tx.isIncome() ? "Income" : "Expense");
        int rows = db.update(TABLE_TRANSACTIONS, cv, T_COL_ID + "=?", new String[]{String.valueOf(tx.getId())});
        return rows > 0;
    }

    public boolean deleteTransaction(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_TRANSACTIONS, T_COL_ID + "=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t." + T_COL_ID + ", t." + T_COL_AMOUNT + ", t." + T_COL_NOTE + ", t." + T_COL_DATE +
                ", t." + T_COL_TYPE + ", t." + T_COL_CATEGORY_ID + ", c." + C_COL_NAME + " as category_name " +
                "FROM " + TABLE_TRANSACTIONS + " t " +
                "LEFT JOIN " + TABLE_CATEGORIES + " c ON t." + T_COL_CATEGORY_ID + "=c." + C_COL_ID +
                " ORDER BY t." + T_COL_DATE + " DESC, t." + T_COL_ID + " DESC";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);
        if (c != null) {
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow(T_COL_ID));
                double amount = c.getDouble(c.getColumnIndexOrThrow(T_COL_AMOUNT));
                String note = c.getString(c.getColumnIndexOrThrow(T_COL_NOTE));
                String date = c.getString(c.getColumnIndexOrThrow(T_COL_DATE));
                String type = c.getString(c.getColumnIndexOrThrow(T_COL_TYPE));
                int catId = c.isNull(c.getColumnIndexOrThrow(T_COL_CATEGORY_ID)) ? -1 : c.getInt(c.getColumnIndexOrThrow(T_COL_CATEGORY_ID));
                String catName = c.getString(c.getColumnIndexOrThrow("category_name"));

                Transaction tx = new Transaction(id, note, date, amount, "Income".equals(type), catId, catName);
                list.add(tx);
            }
            c.close();
        }
        return list;
    }

    // Tính tổng Income / Expense / Balance
    public Map<String, Double> getSummary() {
        Map<String, Double> map = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();

        double totalIncome = 0, totalExpense = 0;

        Cursor c = db.rawQuery("SELECT SUM(amount) FROM " + TABLE_TRANSACTIONS + " WHERE " + T_COL_TYPE + "='Income'", null);
        if (c != null && c.moveToFirst()) {
            totalIncome = c.isNull(0) ? 0 : c.getDouble(0);
            c.close();
        }

        Cursor c2 = db.rawQuery("SELECT SUM(amount) FROM " + TABLE_TRANSACTIONS + " WHERE " + T_COL_TYPE + "='Expense'", null);
        if (c2 != null && c2.moveToFirst()) {
            totalExpense = c2.isNull(0) ? 0 : c2.getDouble(0);
            c2.close();
        }

        map.put("income", totalIncome);
        map.put("expense", totalExpense);
        map.put("balance", totalIncome - totalExpense);
        return map;
    }
}
