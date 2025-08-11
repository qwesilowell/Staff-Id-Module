/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.DTO.AccessHistoryDTO;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.service.HistoryDTOService;
import com.margins.STIM.util.DateFormatter;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Named("entryExitBean")
@ViewScoped
@Getter
@Setter
public class EntryExitBean implements Serializable {

    private List<AccessHistoryDTO> accessHistoryList;

    @Inject
    private HistoryDTOService historyDTOService;

    @Inject
    private EntrancesService entranceService;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer selectedEntranceId;

    private List<Entrances> allEntrances;

    @PostConstruct
    public void init() {
        allEntrances = entranceService.findAllEntrances();
    }

    public void loadReports() {
        LocalDate defaultDate = LocalDate.now();

        LocalDate start = (startDate != null) ? startDate : defaultDate;
        LocalDate end = (endDate != null) ? endDate : defaultDate;

        if (startDate == null || endDate == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Date range missing", "Defaulting to today"));
        }

        accessHistoryList = historyDTOService.generateAccessHistory(start, end, selectedEntranceId);

    }

    public void reset() {
        startDate = null;
        endDate = null;
        selectedEntranceId = null;
    }

    public String formatDuration(Duration duration) {
        if (duration == null) {
            return "N/A";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.toSeconds() % 60;

        StringBuilder result = new StringBuilder();
        if (hours > 0) {
            result.append(hours).append("h ");
        }
        if (minutes > 0 || hours == 0) {
            result.append(minutes).append("m ");
        }
        if (seconds > 0 || hours == 0 && minutes == 0) {
            result.append(seconds).append("s");
        }

        return result.toString().trim();
    }
    
    public String formatTime(LocalDateTime time) {
        if (time != null) {
            return DateFormatter.forDateTime(time);
        }
        return "No Record";
    }
}
