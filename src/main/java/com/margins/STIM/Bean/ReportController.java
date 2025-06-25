/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter 
@Setter
@ViewScoped
@Named("reportController")
public class ReportController implements Serializable  {
    @Inject
    private BreadcrumbBean breadcrumbBean;
    
    public void init (){
    
    setupRepBreadcrumb();
    }
    
    public void setupRepBreadcrumb(){
    breadcrumbBean.setReportsBreadcrumb(); 
    }
}
