/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Charts;

import com.margins.STIM.DTO.EntranceAccessStatsDTO;
import com.margins.STIM.service.AccessLogService;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter

public class LineCharts implements Serializable {

    @Inject
    private AccessLogService accessLogService;

    public LineChartModel buildEntryExitLineChart(LocalDateTime start, LocalDateTime end) {

        LineChartModel model = new LineChartModel();
        ChartData data = new ChartData();

        List<EntranceAccessStatsDTO> statsList = accessLogService.getTop5EntranceAccessStats(start, end);

        if (statsList == null || statsList.isEmpty()) {
            System.out.println("No entry/exit stats data found.");
            return new LineChartModel();
        }

        List<String> labels = new ArrayList<>();
        List<Object> entryCounts = new ArrayList<>();
        List<Object> exitCounts = new ArrayList<>();

        for (EntranceAccessStatsDTO dto : statsList) {
            labels.add(dto.getEntrance().getEntranceName()); // or dto.getEntranceName() if available
            entryCounts.add(dto.getEntryCount());
            exitCounts.add(dto.getExitCount());
        }

        // Dataset for Entries
        LineChartDataSet entrySet = new LineChartDataSet();
        entrySet.setLabel("ENTRIES");
        entrySet.setData(entryCounts);
        entrySet.setBorderColor("#42A5F5"); // blue
        entrySet.setFill(false);

        // Dataset for Exits
        LineChartDataSet exitSet = new LineChartDataSet();
        exitSet.setLabel("EXITS");
        exitSet.setData(exitCounts);
        exitSet.setBorderColor("#FF9800"); // orange
        exitSet.setFill(false);

        data.setLabels(labels);
        data.addChartDataSet(entrySet);
        data.addChartDataSet(exitSet);

        model.setData(data);

        // Chart Options
        LineChartOptions options = new LineChartOptions();
        options.setResponsive(true);
        options.setMaintainAspectRatio(false);
        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("right");
        options.setLegend(legend);

        model.setOptions(options);

        return model;
    }

}
