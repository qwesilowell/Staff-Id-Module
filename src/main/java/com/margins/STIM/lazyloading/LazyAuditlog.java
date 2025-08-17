/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.lazyloading;

import com.margins.STIM.entity.AuditLog;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.service.AuditLogService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 *
 * @author PhilipManteAsare
 */
@AllArgsConstructor
public class LazyAuditlog extends LazyDataModel<AuditLog> {

    private final AuditLogService auditLogService;

    private final String details;

    private final List<LocalDateTime> timeRange;

    private final ActionResult selectedResult;

    private final Users selectedUser;

    private final AuditActionType selectedAction;


    @Override

    public int count(Map<String, FilterMeta> filterBy) {

        return auditLogService.countFilterAuditLogs(timeRange, selectedAction,selectedResult,selectedUser, details);

    }

    @Override
    public List<AuditLog> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {

        // Extract sort information
        String sortField = null;

        SortOrder sortOrder = SortOrder.DESCENDING; // Default

        if (sortBy != null && !sortBy.isEmpty()) {

            SortMeta sortMeta = sortBy.values().iterator().next();

            sortField = sortMeta.getField();

            sortOrder = sortMeta.getOrder();

        }

        // Load data with pagination
        List<AuditLog> data = auditLogService.filterAuditLogs(timeRange, selectedAction,selectedResult,selectedUser, details, first, pageSize, sortField, sortOrder);

        // Set row count for pagination
        if (getRowCount() <= 0) {

            setRowCount(auditLogService.countFilterAuditLogs(timeRange, selectedAction,selectedResult,selectedUser, details));

        }

        return data;

    }

    @Override

    public String getRowKey(AuditLog object) {

        return object.getId().toString();

    }

    @Override

    public AuditLog getRowData(String rowKey) {

        if (rowKey == null || rowKey.isEmpty()) {

            return null;

        }

        Long id = Long.valueOf(rowKey);
        return auditLogService.findAuditById(id);
    }

}
