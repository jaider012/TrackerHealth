package com.example.trackerhealth.helpers;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.example.trackerhealth.model.Meal;
import com.example.trackerhealth.model.PhysicalActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Helper class for generating charts for the reports screen
 */
public class ChartHelper {

    /**
     * Set up and populate an activity chart
     * @param chart The chart view
     * @param activities List of activities to display
     */
    public static void setupActivityChart(View chartView, List<PhysicalActivity> activities) {
        if (chartView instanceof BarChart) {
            BarChart chart = (BarChart) chartView;
            
            // Group activities by date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
            
            Map<String, Double> activityByDate = new TreeMap<>();
            List<String> dateLabels = new ArrayList<>();
            
            // Initialize with last 7 days
            Calendar calendar = Calendar.getInstance();
            for (int i = 6; i >= 0; i--) {
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                String dateKey = dateFormat.format(calendar.getTime());
                String displayDate = displayFormat.format(calendar.getTime());
                activityByDate.put(dateKey, 0.0);
                dateLabels.add(displayDate);
            }
            
            // Add activity data
            for (PhysicalActivity activity : activities) {
                String dateKey = activity.getDate();
                if (activityByDate.containsKey(dateKey)) {
                    double currentValue = activityByDate.get(dateKey);
                    activityByDate.put(dateKey, currentValue + activity.getDuration());
                }
            }
            
            // Prepare chart data
            List<BarEntry> entries = new ArrayList<>();
            int index = 0;
            for (Double value : activityByDate.values()) {
                entries.add(new BarEntry(index++, value.floatValue()));
            }
            
            BarDataSet dataSet = new BarDataSet(entries, "Activity Minutes");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            
            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.9f);
            
            chart.setData(barData);
            chart.getDescription().setEnabled(false);
            chart.getLegend().setEnabled(false);
            
            // X-axis formatting
            XAxis xAxis = chart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            
            // Y-axis formatting
            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setDrawGridLines(true);
            leftAxis.setAxisMinimum(0f);
            
            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setEnabled(false);
            
            chart.animateY(1000);
            chart.invalidate();
        }
    }
    
    /**
     * Set up and populate a nutrition chart
     * @param chart The chart view
     * @param meals List of meals to display
     */
    public static void setupNutritionChart(View chartView, List<Meal> meals) {
        if (chartView instanceof PieChart) {
            PieChart chart = (PieChart) chartView;
            
            // Calculate total macronutrients
            double totalProteins = 0;
            double totalCarbs = 0;
            double totalFats = 0;
            
            for (Meal meal : meals) {
                totalProteins += meal.getProteins();
                totalCarbs += meal.getCarbs();
                totalFats += meal.getFats();
            }
            
            // Prepare chart data
            List<PieEntry> entries = new ArrayList<>();
            
            if (totalProteins > 0) entries.add(new PieEntry((float) totalProteins, "Proteins"));
            if (totalCarbs > 0) entries.add(new PieEntry((float) totalCarbs, "Carbs"));
            if (totalFats > 0) entries.add(new PieEntry((float) totalFats, "Fats"));
            
            // If no data, add placeholder
            if (entries.isEmpty()) {
                entries.add(new PieEntry(1f, "No Data"));
            }
            
            PieDataSet dataSet = new PieDataSet(entries, "Macronutrients");
            dataSet.setColors(new int[] {
                Color.rgb(46, 204, 113),  // Green for proteins
                Color.rgb(52, 152, 219),  // Blue for carbs
                Color.rgb(231, 76, 60)    // Red for fats
            });
            
            PieData pieData = new PieData(dataSet);
            pieData.setValueTextSize(14f);
            pieData.setValueTextColor(Color.WHITE);
            
            chart.setData(pieData);
            chart.getDescription().setEnabled(false);
            chart.setHoleRadius(40f);
            chart.setTransparentCircleRadius(45f);
            chart.animateY(1000);
            chart.invalidate();
        }
    }
    
    /**
     * Set up and populate a calories chart
     * @param chart The chart view
     * @param meals List of meals to display
     */
    public static void setupCaloriesChart(View chartView, List<Meal> meals) {
        if (chartView instanceof LineChart) {
            LineChart chart = (LineChart) chartView;
            
            // Group calories by date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
            
            Map<String, Integer> caloriesByDate = new TreeMap<>();
            List<String> dateLabels = new ArrayList<>();
            
            // Initialize with last 7 days
            Calendar calendar = Calendar.getInstance();
            for (int i = 6; i >= 0; i--) {
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                String dateKey = dateFormat.format(calendar.getTime());
                String displayDate = displayFormat.format(calendar.getTime());
                caloriesByDate.put(dateKey, 0);
                dateLabels.add(displayDate);
            }
            
            // Add meal data
            for (Meal meal : meals) {
                String dateKey = meal.getDate();
                if (caloriesByDate.containsKey(dateKey)) {
                    int currentValue = caloriesByDate.get(dateKey);
                    caloriesByDate.put(dateKey, currentValue + meal.getCalories());
                }
            }
            
            // Prepare chart data
            List<Entry> entries = new ArrayList<>();
            int index = 0;
            for (Integer value : caloriesByDate.values()) {
                entries.add(new Entry(index++, value));
            }
            
            LineDataSet dataSet = new LineDataSet(entries, "Daily Calories");
            dataSet.setColor(Color.rgb(255, 165, 0));
            dataSet.setLineWidth(2f);
            dataSet.setCircleColor(Color.rgb(255, 165, 0));
            dataSet.setCircleRadius(4f);
            dataSet.setDrawCircleHole(false);
            dataSet.setValueTextSize(9f);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(Color.rgb(255, 165, 0));
            dataSet.setFillAlpha(65);
            
            LineData lineData = new LineData(dataSet);
            
            chart.setData(lineData);
            chart.getDescription().setEnabled(false);
            
            // X-axis formatting
            XAxis xAxis = chart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            
            // Y-axis formatting
            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setDrawGridLines(true);
            leftAxis.setAxisMinimum(0f);
            
            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setEnabled(false);
            
            chart.animateX(1000);
            chart.invalidate();
        }
    }
} 