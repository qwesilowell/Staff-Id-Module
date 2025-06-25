/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Charts;

import com.margins.STIM.enums.ChartColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;

/**
 *
 * @author PhilipManteAsare
 */
public class BaseLineChart {

    public static LineChartModel generateChart(Map<String, Integer> mapData) {
        LineChartModel lineModel = new LineChartModel();
        ChartData data = new ChartData();

        LineChartDataSet dataSet = new LineChartDataSet();
        List<Object> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        mapData.forEach((key, value) -> {
            values.add(value);
            labels.add(key);
        });

        dataSet.setData(values);
        dataSet.setFill(false);
        dataSet.setLabel("Number of Granted Accesses");
        dataSet.setBorderColor(ChartColor.BLUE.toString());
        dataSet.setTension(0.1);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        //Options
        LineChartOptions options = new LineChartOptions();
        options.setMaintainAspectRatio(false);
        CartesianScales scales = new CartesianScales();
        CartesianLinearAxes yAxes = new CartesianLinearAxes();
        CartesianLinearTicks ticks = new CartesianLinearTicks();

        ticks.setStepSize(1);       // Only step in whole numbers
        ticks.setPrecision(0);      // Optional: 0 decimal places
        yAxes.setTicks(ticks);
        scales.addYAxesData(yAxes);
        options.setScales(scales);

        lineModel.setOptions(options);
        lineModel.setData(data);

        return lineModel;
    }
}
