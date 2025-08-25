/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Scheduler;

import com.margins.STIM.service.AnomalyDetectionService;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;


/**
 *
 * @author PhilipManteAsare
 */
@Singleton
@Startup
public class AnomalyDetectionScheduler {

    @Inject
    private AnomalyDetectionService anomalyDetectionService;

    /**
     * This is scheduled to run every 30 mins to get all entries without an exit
     * in 12 hrs
     */
    @Schedule(second = "0", minute = "0,10", hour = "*", persistent = false)  // Every 10mins
    public void runUnmatchedEntryAnomalyCheck() {
        anomalyDetectionService.detectUnmatchedEntriess();
    }
}
