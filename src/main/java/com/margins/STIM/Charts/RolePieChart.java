/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Charts;

import com.margins.STIM.enums.ChartColor;
import com.margins.STIM.DTO.RoleCount;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.pie.PieChartOptions;

/**
 *
 * @author PhilipManteAsare
 */
public class RolePieChart {

    public static PieChartModel generatechart(List<RoleCount> rc) {
        PieChartModel pieModel = new PieChartModel();
        ChartData data = new ChartData();

        PieChartDataSet dataSet = new PieChartDataSet();
        
        List<Number> values = new ArrayList<>();
        List<String> bgColors = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        int i = 0;
        for(RoleCount roleCount : rc){
            values.add(roleCount.getCount());
            labels.add(roleCount.getRoleName());
            bgColors.add(ChartColor.values()[i % 5].toString());
            i++;
        }
       
        dataSet.setData(values);
        dataSet.setBackgroundColor(bgColors);

        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        PieChartOptions options = new PieChartOptions();
        options.setMaintainAspectRatio(false);

        pieModel.setOptions(options);
        pieModel.setData(data);

        return pieModel;
    }
}
