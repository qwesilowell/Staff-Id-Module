/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.margins.STIM.report.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Edwin
 */
public class ReportData implements Serializable
{
    private String reportTitle;
    private List listData;
    private Object object;
    private Map<String,Object> map = new HashMap<>();
    private String reportFile;
    private String reportFileName;
    private String msg = "";
    
    

    public ReportData()
    {
    }
    
    public ReportData(String msg)
    {
        this.msg = msg;
    }

    public ReportData(List listData, Map<String,Object> map)
    {
        this.listData = listData;
        this.map = map;
    }

    public ReportData(List listData)
    {
        this.listData = listData;
    }
    
    
    public ReportData(Object object, Map<String,Object> map)
    {
        this.object = object;
        this.map = map;
    }
    
    public ReportData(Object object, String reportTitle)
    {
        this.object = object;
        listData = new ArrayList(0);
        listData.add(object);
        this.reportTitle = reportTitle;
        map.put("reportTitle", reportTitle);
    }
    
    public ReportData(List listData, String reportTitle)
    {
        this.listData = listData;
        this.reportTitle = reportTitle;
        map.put("reportTitle", reportTitle);
    }
    
    public boolean containsData()
    {
        return (listData != null) && (listData.isEmpty() == false);
    }
    
    public void addParam(Map<String,Object> parametersMap)
    {
        map.putAll(parametersMap);
    }
    
    public void addParam(String paramName, Object value)
    {
        map.put(paramName, value);
    }

    public String getReportTitle()
    {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle)
    {
        this.reportTitle = reportTitle;
    }

    public void setListData(List listData)
    {
        this.listData = listData;
    }

    public List getListData()
    {
        return listData;
    }

    public Object getObject()
    {
        return object;
    }

    public Map<String, Object> getMap()
    {
        return map;
    }

    public void setReportFile(String reportFile)
    {
        this.reportFile = reportFile;
    }

    public String getReportFile()
    {
        return reportFile;
    }

    public String getReportFileName()
    {
        return reportFileName;
    }

    public void setReportFileName(String reportFileName)
    {
        this.reportFileName = reportFileName;
    }

    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }
    
    
}
