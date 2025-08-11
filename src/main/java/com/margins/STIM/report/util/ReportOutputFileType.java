/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.margins.STIM.report.util;

/**
 *
 * @author Edwin
 */
public enum ReportOutputFileType {

    PDF("application/pdf"),
    XHTML("text/html"),
    EXCEL("text/excel");

    private ReportOutputFileType(String contentType) {
        this.contentType = contentType;
    }



   private String contentType;

    public String getContentType() {
        return contentType;
    }

   
}
