package com.example.expensemanagement;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class StatisticsFragment extends Fragment {

    private PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        pieChart = view.findViewById(R.id.pieChart);
        setupPieChart();

        return view;
    }

    private void setupPieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(500000, "Ăn uống"));
        entries.add(new PieEntry(300000, "Đi lại"));
        entries.add(new PieEntry(200000, "Mua sắm"));
        entries.add(new PieEntry(100000, "Khác"));

        PieDataSet dataSet = new PieDataSet(entries, "Chi tiêu");
        dataSet.setColors(new int[]{Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA});
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(12f);

        pieChart.animateY(1000);
        pieChart.invalidate(); // refresh
    }
}
