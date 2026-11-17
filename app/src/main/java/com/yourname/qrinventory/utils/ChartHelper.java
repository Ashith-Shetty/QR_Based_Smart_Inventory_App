package com.yourname.qrinventory.utils;

import android.graphics.Color;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.color.MaterialColors;
import com.yourname.qrinventory.model.CategoryStock;
import com.yourname.qrinventory.model.DailySales;
import com.yourname.qrinventory.model.TopSeller;
import java.util.ArrayList;
import java.util.List;

public class ChartHelper {

    // Text colour that works in both light and dark mode
    private static int textColor(android.view.View v) {
        return MaterialColors.getColor(v, com.google.android.material.R.attr.colorOnSurface, Color.GRAY);
    }

    public static void showStockPie(PieChart chart, List<CategoryStock> data) {
        int text = textColor(chart);
        chart.getDescription().setEnabled(false);
        if (data == null || data.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No stock data yet");
            chart.setNoDataTextColor(text);
            return;
        }
        List<PieEntry> entries = new ArrayList<>();
        for (CategoryStock c : data) entries.add(new PieEntry(c.totalStock, c.category));
        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setValueTextSize(12f);
        set.setValueTextColor(Color.WHITE);
        chart.setData(new PieData(set));
        chart.setCenterText("Stock by category");
        chart.setCenterTextColor(text);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setEntryLabelColor(text);
        chart.setEntryLabelTextSize(11f);
        chart.getLegend().setTextColor(text);
        chart.getLegend().setWordWrapEnabled(true);
        chart.animateY(800);
        chart.invalidate();
    }

    public static void showTopSellersBar(BarChart chart, List<TopSeller> data) {
        int text = textColor(chart);
        chart.getDescription().setEnabled(false);
        if (data == null || data.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No sales yet");
            chart.setNoDataTextColor(text);
            return;
        }
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entries.add(new BarEntry(i, data.get(i).totalSold));
            labels.add(data.get(i).productName);
        }
        BarDataSet set = new BarDataSet(entries, "Units sold");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setValueTextColor(text);
        set.setValueTextSize(11f);
        set.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) { return String.valueOf((int) value); }
        });
        BarData bd = new BarData(set);
        bd.setBarWidth(0.6f);
        chart.setData(bd);

        XAxis x = chart.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setGranularity(1f);
        x.setLabelCount(labels.size());
        x.setLabelRotationAngle(-35f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setTextColor(text);
        chart.getAxisLeft().setTextColor(text);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(text);
        chart.setExtraBottomOffset(24f);
        chart.animateY(800);
        chart.invalidate();
    }

    public static void showDailySalesLine(LineChart chart, List<DailySales> data) {
        int text = textColor(chart);
        chart.getDescription().setEnabled(false);
        if (data == null || data.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No sales yet");
            chart.setNoDataTextColor(text);
            return;
        }
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entries.add(new Entry(i, (float) data.get(i).total));
            String day = data.get(i).day;
            labels.add(day != null && day.length() >= 10 ? day.substring(5) : day);   // "2026-11-17" -> "11-17"
        }
        LineDataSet set = new LineDataSet(entries, "Daily sales (Rs.)");
        set.setColor(Color.parseColor("#6750A4"));
        set.setCircleColor(Color.parseColor("#6750A4"));
        set.setLineWidth(2.5f);
        set.setCircleRadius(4f);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawFilled(true);
        set.setFillColor(Color.parseColor("#6750A4"));
        set.setFillAlpha(60);
        set.setValueTextColor(text);
        set.setValueTextSize(10f);
        chart.setData(new LineData(set));

        XAxis x = chart.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setGranularity(1f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setTextColor(text);
        chart.getAxisLeft().setTextColor(text);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(text);
        chart.animateX(800);
        chart.invalidate();
    }
}
