/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Charts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;

/**
 *
 * @author PhilipManteAsare
 */
public class EntranceAccessBarChart {

    public static BarChartModel generateChart(Map<String, Integer> mapData) {
        BarChartModel barModel = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet barDataSet = new BarChartDataSet();
        barDataSet.setLabel("VERIFICATION OUTCOMES ");

        List<Number> values = new ArrayList<>();
        List<String> bgColor = new ArrayList<>();
        List<String> borderColor = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        mapData.forEach((key, value) -> {
            values.add(value);
            labels.add(key);
            // Hardcode colors based on key
            if (key.equalsIgnoreCase("Granted")) {
                bgColor.add("rgba(64, 175, 115, 0.8)"); // Green for granted
                borderColor.add("rgb(64, 175, 115)");
            } else if (key.equalsIgnoreCase("Denied")) {
                bgColor.add("rgba(255, 99, 132, 0.8)"); // Red for denied
                borderColor.add("rgb(255, 99, 132)");
            } else {
                // Fallback colors for unexpected keys
                bgColor.add("rgba(128, 128, 128, 0.8)"); // Gray
                borderColor.add("rgb(128, 128, 128)");
            }
        });

        barDataSet.setData(values);
        barDataSet.setBackgroundColor(bgColor);
        barDataSet.setBorderColor(borderColor);
        barDataSet.setBorderWidth(1);


        data.addChartDataSet(barDataSet);
        data.setLabels(labels);
    

        barModel.setData(data);

        // Options
        BarChartOptions options = new BarChartOptions();
        options.setMaintainAspectRatio(false);
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setOffset(true);
        linearAxes.setBeginAtZero(true);
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        ticks.setStepSize(1); // Only step in whole numbers
        ticks.setPrecision(0); // Optional: 0 decimal places
        linearAxes.setTicks(ticks);
        
        
        
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);

        barModel.setOptions(options);

        return barModel;
    }
}
