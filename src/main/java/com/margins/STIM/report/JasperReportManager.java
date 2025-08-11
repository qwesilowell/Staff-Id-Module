/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.margins.STIM.report;

import com.margins.STIM.report.util.ReportDesignFileType;
import com.margins.STIM.report.util.ReportOutputEnvironment;
import com.margins.STIM.report.util.ReportOutputFileType;
import static com.margins.STIM.report.util.ReportOutputFileType.PDF;
import jakarta.faces.context.ExternalContext;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.commons.lang3.StringUtils;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;

/**
 *
 * @author Edwin
 */
public class JasperReportManager implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(JasperReportManager.class.getName());

    private final Map<String, Object> defaultParamenters = new HashMap<>();
    private final Map<String, Object> reportParamenters = new HashMap<>();
    private ReportOutputFileType reportOutputFileType = ReportOutputFileType.PDF;
    ;
    private ReportDesignFileType reportFileType;
    private ReportOutputEnvironment reportOutputEnvironment;
    private JasperPrint jasperPrint;
    private JRBeanCollectionDataSource jrCollectionDataSource = null;
    private String jasperFile;
    private Collection reportDataList;

    private File generatedFile;
    private String reportTitle;

    String msg = "";
    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    public static final String REPORT_TITLE = "reportTitle";

    public void addMap(Map reportParameters) {
        for (Object key : reportParameters.keySet()) {
            addParam(key.toString(), reportParameters.get(key));
        }
    }

    public boolean isVariableSet() {
        return true;
    }

    public void showReport(Collection reportData, InputStream inputStream) {
        this.reportDataList = reportData;
        createJasperPrint(inputStream);
        outputReport();
    }

    public void showReport(Collection reportData, InputStream inputStream, ReportOutputFileType type) {
        this.reportDataList = reportData;
        createJasperPrint(inputStream);
        outputReport(type);
    }

    public String writeToFile(Collection collection, InputStream inputStream, String reportDirectory, String fileName) {
        reportDirectory = reportDirectory.trim();

        if (fileName == null || reportDirectory == null) {
            msg = """
                  Please report directory and file name can not be NULL 
                  REPORT_DIRECTORY = """ + reportDirectory + "\n"
                    + "FILE_NAME = " + fileName;

            Logger.getLogger(JasperReportManager.class.getName()).log(Level.INFO, msg);

            return null;

        }

        fileName = checkFileName(fileName);

        try {
            new File(reportDirectory.trim()).mkdirs();
        } catch (Exception e) {
            msg = "Unable to create or find Report Output directory (" + reportDirectory + ")";
            Logger.getLogger(JasperReportManager.class.getName()).log(Level.SEVERE, msg + "\n" + e.getMessage(), e);

            return null;
        }

        File file = new File(reportDirectory, fileName);
        this.reportDataList = collection;

        createJasperPrint(inputStream);

        msg = "Report will be written to " + file.getAbsolutePath();
        Logger.getLogger(JasperReportManager.class.getName()).log(Level.INFO, msg);

        switch (reportOutputFileType) {
            case PDF -> {
                try {
                    JasperExportManager.exportReportToPdfFile(jasperPrint, file.getAbsolutePath());
                    LOGGER.log(Level.INFO, "Report exported to pdf completed - {0}", file.getAbsolutePath());
                    setGeneratedFile(file);
                    return file.toString();

                } catch (JRException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(JasperReportManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }
            }

        }

        return null;
    }

    private void outputReport() {
        if (jasperPrint == null) {
            msg = "Could not create Jasper Print so Report Process will be abborted";
            Logger.getLogger(JasperReportManager.class.getName()).log(Level.SEVERE, msg);

            return;
        }

        switch (reportOutputEnvironment) {
            case WEB_APPLICATION ->
                webenviro();
            case DESKTOP_APPLICATION ->
                desktopEnviroment();
        }
    }

    private void outputReport(ReportOutputFileType type) {
        if (jasperPrint == null) {
            msg = "Could not create Jasper Print so Report Process will be abborted";
            Logger.getLogger(JasperReportManager.class.getName()).log(Level.SEVERE, msg);

            return;
        }

        switch (reportOutputEnvironment) {
            case WEB_APPLICATION ->
                webenviro(type);
            case DESKTOP_APPLICATION ->
                desktopEnviroment();
        }
    }

    private void webenviro(ReportOutputFileType type) {
        HttpServletResponse response = getServeltResponse();
        HttpServletRequest request = getServeltRequest();

        if (type == null) {
            LOGGER.warning("Report output type not set, defaulting to PDF");
            type = ReportOutputFileType.PDF;
        }

        try {
            // Set common headers
            response.reset();

            switch (type) {
                case PDF:
                    response.setContentType("application/pdf");
                    response.setHeader("Content-Disposition", "inline; filename=\"report.pdf\"");

                    JRPdfExporter pdfExporter = new JRPdfExporter();
                    pdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
                    pdfExporter.exportReport();
                    break;

                case EXCEL:
                    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    response.setHeader("Content-Disposition", "attachment; filename=\"report.xlsx\"");

                    JRXlsxExporter excelExporter = new JRXlsxExporter();
                    excelExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    excelExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
                    excelExporter.exportReport();
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported export type: " + type);
            }

            FacesContext.getCurrentInstance().responseComplete();

        } catch (IOException | IllegalArgumentException | JRException e) {
            LOGGER.log(Level.SEVERE, "Error generating report: " + e.getMessage(), e);
        }
    }

    private void webenviro() {

        HttpServletResponse response = getServeltResponse();
        HttpServletRequest request = getServeltRequest();

//        response.setContentType(reportOutput.getContentType());
        if (reportOutputFileType == null) {
            LOGGER.info("Report outfile type is not set, PDF will be assumed");
            reportOutputFileType = ReportOutputFileType.PDF;
        }

        switch (reportOutputFileType) {
            case PDF -> {
                try {
                    exportPdfReport(jasperPrint);
                } catch (IOException | JRException ex) {
                    Logger.getLogger(JasperReportManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        try {
            FacesContext.getCurrentInstance().responseComplete();
        } catch (Exception e) {
            Logger.getLogger(JasperReportManager.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }

    }

    public void exportPdfReport(JasperPrint jasperPrint) throws IOException, JRException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

        response.reset();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"report.pdf\"");

        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
        exporter.exportReport();

        facesContext.responseComplete();
    }

    private void desktopEnviroment() {

        JasperViewer jasperViewer = new JasperViewer(jasperPrint, false);

        try {
            reportTitle = (String) reportParamenters.get(REPORT_TITLE);
        } catch (Exception e) {
        }

        jasperViewer.setTitle(reportTitle);
        jasperViewer.setVisible(true);

    }

    public void addParam(String paramKey, Object paramValue) {
        reportParamenters.put(paramKey, paramValue);
    }

    public static HttpServletResponse getServeltResponse() {
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().
                getExternalContext().getResponse();

        return response;
    }

    public static HttpServletRequest getServeltRequest() {
        HttpServletRequest response = (HttpServletRequest) FacesContext.getCurrentInstance().
                getExternalContext().getRequest();

        return response;
    }

    public String checkFileName(String fileName) {
        String oldFilename = fileName;

        char[] chrs = new char[]{'\'', '/', ':', '*', '?', '"', '<', '>', '|'};
        if (!StringUtils.containsAny(fileName, chrs)) {
            return fileName;
        }

        for (int i = 0; i < chrs.length; i++) {
            char c = chrs[i];
            fileName = StringUtils.remove(fileName, c);
        }

        msg = "Report Output File Name (" + oldFilename + ") contains escape or invalid charaters. "
                + "Will be replaced with (-) to " + fileName;
        Logger.getLogger(JasperReportManager.class.getName()).log(Level.INFO, msg);
        return fileName;
    }

    private void createJasperPrint(InputStream rptIns) {
        try {
            jrCollectionDataSource = new JRBeanCollectionDataSource(reportDataList);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        if (reportFileType == null) {
            throw new IllegalArgumentException("Please specify report file type : " + reportFileType);
        }

        if (reportFileType == ReportDesignFileType.INPUTSTREAM) {
            InputStream inputStream = null;

            if (rptIns != null) {
                inputStream = rptIns;
            } else {
                try {
                    if (!jasperFile.endsWith(".jasper")) {
                        jasperFile = jasperFile + ".jasper";
                    }
                    inputStream = this.getClass().getClassLoader().getResourceAsStream(jasperFile);
                    System.out.println(inputStream + " result of searchin.... " + jasperFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    msg = "Unable to load Input Stream for " + jasperFile;
                    Logger.getLogger(JasperReportManager.class.getName()).log(Level.SEVERE, msg + " \n " + e.toString(), e);

                    jasperPrint = null;

                    return;
                }
            }

            try {
                msg = "Creating JasperPrint with inputstream: " + inputStream
                        + " and bean collection datasource " + jrCollectionDataSource.toString();

                Logger.getLogger(JasperReportManager.class.getName()).log(Level.INFO, msg);

                if (inputStream == null) {
                    return;
                }

                System.out.println("going to create jasper print from : " + inputStream);
                jasperPrint = JasperFillManager.fillReport(inputStream, reportParamenters, jrCollectionDataSource);

                System.out.println("jasperPrint created = " + jasperPrint);

            } catch (JRException e) {
                Logger.getLogger(JasperReportManager.class.getName()).log(Level.SEVERE,
                        "Error Creating JasperPrint for " + jasperFile + "\n" + e.toString(), e);
                e.printStackTrace();

            }
        } else if (reportFileType == ReportDesignFileType.STRING_FILE) {
        }
    }

    public void addToDefaultParameters(String paramKey, Object paramValue) {
        defaultParamenters.put(paramKey, paramValue);
        reportParamenters.put(paramKey, paramValue);
    }

    public void resetReportParametersToDefault() {
        reportParamenters.clear();
        reportParamenters.putAll(defaultParamenters);
    }

    public Map<String, Object> getReportParamenters() {
        return reportParamenters;
    }

    public ReportOutputEnvironment getReportEnvironment() {
        return reportOutputEnvironment;
    }

    public void setReportEnvironment(ReportOutputEnvironment reportEnvironment) {
        this.reportOutputEnvironment = reportEnvironment;
    }

    public ReportDesignFileType getReportFileType() {
        return reportFileType;
    }

    public void setReportFileType(ReportDesignFileType reportFileType) {
        this.reportFileType = reportFileType;
    }

    public ReportOutputFileType getReportOutput() {
        return reportOutputFileType;
    }

    public void setReportOutput(ReportOutputFileType reportOutput) {
        this.reportOutputFileType = reportOutput;
    }

    public File getGeneratedFile() {
        return generatedFile;
    }

    public void setGeneratedFile(File generatedFile) {
        this.generatedFile = generatedFile;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

}
