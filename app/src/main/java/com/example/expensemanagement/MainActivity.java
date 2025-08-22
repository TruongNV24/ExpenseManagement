package com.example.expensemanagement;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Toast.makeText(MainActivity.this, "Home selected", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_transactions) {
                    Toast.makeText(MainActivity.this, "Transactions selected", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_statistics) {
                    Toast.makeText(MainActivity.this, "Statistics selected", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }
}
