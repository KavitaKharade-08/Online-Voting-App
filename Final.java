package com.example.omg;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class Final extends AppCompatActivity {

    TextView voterIdText;
    PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);

        voterIdText = findViewById(R.id.voterIdText);
        pieChart = findViewById(R.id.pieChart);
        ImageView appLogo = findViewById(R.id.appLogo);

        // Get data from Intent
        String voterId = getIntent().getStringExtra("voterId");
        int votedCount = getIntent().getIntExtra("count",0 );

        // Set Voter ID text
        voterIdText.setText("Voter ID: " + voterId);

        // Assume total voters (e.g., 20) – replace with actual DB value if needed
        int totalVoters = 20;

        // Ensure that votedCount is at least 12% of total voters
        int minimumVotedCount = (int) (totalVoters * 0.13);  // 13% of total voters
        votedCount = Math.max(votedCount, minimumVotedCount);

        int remainingCount = totalVoters - votedCount;

        // Set up Pie Chart entries
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(votedCount, "Voted"));
        entries.add(new PieEntry(remainingCount, "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "Voting Progress");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);

        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Voting Status");
        pieChart.setCenterTextSize(18f);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);

        // Customize legend
        Legend legend = pieChart.getLegend();
        legend.setTextSize(14f);
        legend.setFormSize(14f);
        legend.setForm(Legend.LegendForm.CIRCLE);

        pieChart.invalidate(); // Refresh chart
    }
}
