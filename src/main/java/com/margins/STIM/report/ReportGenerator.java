/*
 * To change this license header, choose Licence Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.margins.STIM.report;

import com.margins.STIM.report.util.ReportDesignFileType;
import com.margins.STIM.report.util.ReportOutputEnvironment;
import com.margins.STIM.report.util.ReportOutputFileType;
import java.io.Serializable;

/**
 *
 * @author Duker
 */
public class ReportGenerator extends JasperReportManager implements Serializable {

    private static final String REPORT_BASE_DIR = "/WEB-INF/reports/";
    public static final String EMPLOYEE_REPORT = REPORT_BASE_DIR + "EmployeeReport.jasper";
    public static final String ACCESSIBLE_EMPLOYEE_ENTRANCES = REPORT_BASE_DIR + "Accesible_Entrances.jasper";
    public static final String EMPLOYEES_ROLES_REPORT = REPORT_BASE_DIR + "RolesReport.jasper";
    public static final String EMPLOYEES_IN_ROLE = REPORT_BASE_DIR + "EmployeesInRole.jasper";
    public static final String ACCESSIBLE_ENTRANCES_PER_ROLE = REPORT_BASE_DIR + "AccessibleEntrancesPerRole.jasper";
    public static final String ENTRANCES_REPORT = REPORT_BASE_DIR + "EntranceReport.jasper";
    public static final String ENTRANCES_INFORMATION = REPORT_BASE_DIR + "EntranceInformation.jasper";

    public ReportGenerator() {
        setReportEnvironment(ReportOutputEnvironment.WEB_APPLICATION);
        setReportFileType(ReportDesignFileType.INPUTSTREAM);
        setReportOutput(ReportOutputFileType.PDF);
    }
}
