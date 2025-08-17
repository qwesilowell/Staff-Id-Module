/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.DTO.AccessHistoryDTO;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.report.ReportGenerator;
import com.margins.STIM.report.ReportManager;
import com.margins.STIM.report.model.EntranceAccessReport;
import com.margins.STIM.report.util.ReportOutputFileType;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

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

    @Inject
    private BreadcrumbBean breadCrumb;

    @Inject
    private ReportManager rm;

    @Inject
    private UserSession userSession;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer selectedEntranceId;
    private String selectedAccessStatus;

    private List<Entrances> allEntrances;

    @PostConstruct
    public void init() {
        allEntrances = entranceService.findAllEntrances();
        LocalDate now = LocalDate.now();
        startDate = now.minusMonths(1);
        endDate = now;
        loadReports();
    }

    public void loadReports() {
        LocalDate defaultDate = LocalDate.now();

        LocalDate start = (startDate != null) ? startDate : defaultDate.minusMonths(5);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        if (startDate == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Start Date range missing", "Defaulting to last month"));
        }
        if (endDate == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "End Date range missing", "Defaulting to today"));
        }

        accessHistoryList = historyDTOService.generateAccessHistory(start, end, selectedEntranceId, selectedAccessStatus);

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
        return "-";
    }

    public String getDisplayDeviceEntryName(AccessHistoryDTO record) {
        return record.getDeviceEntryName() != null ? record.getDeviceEntryName() : "N/A";
    }

    public String getDisplayDeviceExitName(AccessHistoryDTO record) {
        return record.getDeviceExitName() != null ? record.getDeviceExitName() : "N/A";
    }

    public String getEntranceSelected(Integer selectedEntranceId) {
        if (selectedEntranceId != null) {
            Entrances entrance = entranceService.findEntranceById(selectedEntranceId);
            return entrance.getEntranceName();
        }
        return "ALL ENTRANCES";
    }

    public String getSelectedAccessStatus() {
        if (this.selectedAccessStatus == null) {
            return "All Records";
        }
        return this.selectedAccessStatus;
    }

    public String safeString(String value) {
        return value != null ? value : "N/A";
    }
    
    public String getAccessStatus(AccessHistoryDTO record) {
        if (record.getTimeEntered() != null && record.getTimeExited() == null) {
            return "MISSING EXIT";
        }
        if (record.getTimeEntered() == null && record.getTimeExited() != null) {
            return "MISSING ENTRY";
        }
        if (record.getTimeEntered() != null && record.getTimeExited() != null) {
            return "COMPLETE";
        }
        return "N/A"; // If both are null or unexpected case
    }

    public void setupBreadcrumb() {
        breadCrumb.setEntranceAccessReportReportBreadcrumb();
    }

    public void export() {

        List<EntranceAccessReport> entAccessRepo = new ArrayList<>();

        for (AccessHistoryDTO dto : accessHistoryList) {
            EntranceAccessReport ear = new EntranceAccessReport();
            ear.setEmployeeName(dto.getEmployeeName());
            ear.setGhanaCardNumber(dto.getGhanaCardNumber());
            ear.setEntranceName(dto.getEntranceName());
            ear.setDeviceEntryName(getDisplayDeviceEntryName(dto));
            ear.setTimeEntered(formatTime(dto.getTimeEntered()));
            ear.setEntryResult(safeString(dto.getEntryResult()));
            ear.setDeviceExitName(getDisplayDeviceExitName(dto));
            ear.setTimeExited(formatTime(dto.getTimeExited()));
            ear.setExitResult(safeString(dto.getExitResult()));
            ear.setDuration(formatDuration(dto.getDuration()));
            ear.setStatus(getAccessStatus(dto));

            entAccessRepo.add(ear);
        }
        rm.addParam("startDate", DateFormatter.forLocalDate(startDate));
        rm.addParam("endDate", DateFormatter.forLocalDate(endDate));
        rm.addParam("entranceName", getEntranceSelected(selectedEntranceId));
        rm.addParam("printedBy", userSession.getUsername());
        rm.addParam("accessStatus", getSelectedAccessStatus());
        rm.addParam("entranceAccessTable", new JRBeanCollectionDataSource(entAccessRepo));
        rm.setReportFile(ReportGenerator.ENTRANCE_ACCESS_REPORT);
        rm.setReportData(Arrays.asList(new Object()));
        rm.generateReport(ReportOutputFileType.PDF);
    }
}
