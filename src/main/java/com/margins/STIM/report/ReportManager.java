/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.margins.STIM.report;

import com.margins.STIM.report.util.ReportOutputFileType;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.faces.context.FacesContext;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.Setter;
import org.omnifaces.util.Messages;

/**
 *
 * @author Edwin
 */
@Stateless
public class ReportManager implements Serializable {

    private static final Logger LOG = Logger.getLogger(ReportManager.class.getName());

    //@Inject private UserSession userSession;
    ReportGenerator rptManager = new ReportGenerator();
    private String reportTitle;
    private List reportDataList;
    private Object reportData;
    private String reportFile;
    @Getter
    @Setter
    private String reportDirectory;
    @Getter
    @Setter
    private String filename;

    private Map<String, Object> reportParameters = new LinkedHashMap<>();

    /**
     * Creates a new instance of ReportManagerController
     */
    @PostConstruct
    public void ReportManager() {

    }

    public void clear() {
        reportParameters.clear();
        reportTitle = "";
        reportDataList = null;
    }

    public void addParam(String key, Object value) {
        reportParameters.put(key, value);
    }

        public void generateReport(ReportOutputFileType type) {
        try {
            System.out.println("THE PATH >>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + reportFile);
            LOG.log(Level.INFO, "Report File is : {0}", reportFile);
            if (reportFile == null) {
                Messages.addGlobalError("Error in Generating Report");
                return;
            }

            InputStream ins = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(reportFile);
            if (ins != null) {
                System.out.println("READ SUCCESSFUL>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            } else {
                System.out.println("READ IS NULL>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            }

            rptManager.addMap(reportParameters);

            System.out.println(reportParameters);

            
            rptManager.showReport(reportDataList, ins, type);
            //rptManager.showReport(reportDataList, reportFile);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error generating report", e);
            e.printStackTrace();
        }
    }
        
        
    public void generateReport() {
        try {
            System.out.println("THE PATH >>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + reportFile);
            LOG.log(Level.INFO, "Report File is : {0}", reportFile);
            if (reportFile == null) {
                Messages.addGlobalError("Error in Generating Report");
                return;
            }

            InputStream ins = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(reportFile);

            if (ins != null) {
                System.out.println("READ SUCCESSFUL>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            } else {
                System.out.println("READ IS NULL>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            }

            rptManager.addMap(reportParameters);

            System.out.println(reportParameters);

            rptManager.showReport(reportDataList, ins);
//            rptManager.writeToFile(reportDataList, ins, reportDirectory, filename);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error generating report", e);
            e.printStackTrace();
        }
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;

        addParam("reportTitle", reportTitle);
    }

    public void setReportFile(String reportFile) {
        this.reportFile = reportFile;
    }

    public void setReportDataList(List reportDataList) {
        this.reportDataList = reportDataList;
    }

    public void setReportData(Object reportData) {
        this.reportData = reportData;
        List list = new ArrayList(1);
        list.add(reportData);
        reportDataList = list;
    }

    public static BufferedImage loadTranslucentImage(InputStream inputStream) throws IOException {

        if (inputStream == null) {
            System.out.println("Image is null");
            return null;
        }
        float transparency = (float) 0.2;

        // Load the image  
        BufferedImage loaded = ImageIO.read(inputStream);
        // Create the image using the   
        BufferedImage aimg = new BufferedImage(loaded.getWidth(), loaded.getHeight(), BufferedImage.TRANSLUCENT);

        // Get the images graphics  
        Graphics2D g = aimg.createGraphics();
        // Set the Graphics composite to Alpha  
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        // Draw the LOADED img into the prepared reciver image  
        g.drawImage(loaded, null, 0, 0);
        // let go of all system resources in this Graphics  
        g.dispose();
        // Return the image  
        return aimg;
    }

}
