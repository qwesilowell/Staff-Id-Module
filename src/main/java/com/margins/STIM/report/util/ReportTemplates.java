/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.margins.STIM.report.util;

import com.margins.STIM.report.JasperReportManager;

/**
 *
 * @author sabonay
 */
public class ReportTemplates
{

    public static JasperReportManager getWeb_PDF_is()
    {
        JasperReportManager rm = new JasperReportManager();

        rm.setReportEnvironment(ReportOutputEnvironment.WEB_APPLICATION);
        rm.setReportFileType(ReportDesignFileType.INPUTSTREAM);
        rm.setReportOutput(ReportOutputFileType.PDF);

        return rm;
    }


    public static JasperReportManager getWebxhtml()
    {
        JasperReportManager rm = new JasperReportManager();

        rm.setReportEnvironment(ReportOutputEnvironment.WEB_APPLICATION);
        rm.setReportFileType(ReportDesignFileType.INPUTSTREAM);
        rm.setReportOutput(ReportOutputFileType.XHTML);

        return rm;
    }

}
