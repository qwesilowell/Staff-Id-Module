/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author PhilipManteAsare
 */
public class DateFormatter {
    public static String formatDate(Date date) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("d");
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM, yyyy");

        // Extract day and determine the ordinal suffix
        int day = Integer.parseInt(dayFormat.format(date));
        String ordinalSuffix = getOrdinalSuffix(day);

        // Format month and year
        String monthYear = monthYearFormat.format(date);

        // Combine them to get the final formatted date
        return day + ordinalSuffix + " " + monthYear;
    }

    private static String getOrdinalSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th"; // Special case for 11th, 12th, 13th
        }
        switch (day % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }
}
