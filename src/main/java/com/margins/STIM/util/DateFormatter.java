/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.util;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

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

    public static String forDateTime(LocalDateTime dateTime) {
        // Format for day
        int day = dateTime.getDayOfMonth();
        String ordinalSuffix = getOrdinalSuffix(day);

        // Format month and year
        String month = dateTime.format(DateTimeFormatter.ofPattern("MMMM"));
        int year = dateTime.getYear();

        // Format time (24-hour or 12-hour format)
        String time = dateTime.format(DateTimeFormatter.ofPattern("hh:mm a")); // 12-hour
        // for 24-hour: String time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        // Combine them
        return day + ordinalSuffix + " " + month + " " + year + " at " + time;
    }

    public static String forDateTimes(LocalDateTime time) {
        if (time != null) {
            return forDateTime(time);
        }
        return "No Record";
    }

    public static LocalTime toLocalTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime();  //ignores Jan-1970
    }

    public static String forLocalDate(LocalDate ld) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return ld.format(formatter);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }

    private static String getOrdinalSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th"; // Special case for 11th, 12th, 13th
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static Date toDate(LocalTime localTime) {
        if (localTime == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        LocalDateTime ldt = LocalDateTime.of(today, localTime);
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String getFirstChar(String word) {
        return (word != null && word.length() >= 1) ? word.substring(0, 1).toUpperCase() : "?";
    }
    
    public static String formatDateAsTimeString(Date date) {
        if (date == null) {
            return "";
        }

        LocalDateTime dateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDate now = LocalDate.now();
        LocalDate then = dateTime.toLocalDate();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy, h:mm a");
        DayOfWeek dayOfWeek = then.getDayOfWeek();

        long daysBetween = ChronoUnit.DAYS.between(then, now);

        if (daysBetween == 0) {
            return "Today, " + dateTime.format(timeFormatter);
        } else if (daysBetween == 1) {
            return "Yesterday, " + dateTime.format(timeFormatter);
        } else if (daysBetween <= 6) {
            String dayName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            return dayName + ", " + dateTime.format(timeFormatter);
        } else {
            return dateTime.format(fullFormatter);
        }
    }

    public static String formatLocalDateAsTimeString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDate now = LocalDate.now();
        LocalDate then = dateTime.toLocalDate();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy, h:mm a");
        DayOfWeek dayOfWeek = then.getDayOfWeek();

        long daysBetween = ChronoUnit.DAYS.between(then, now);

        if (daysBetween == 0) {
            return "Today, " + dateTime.format(timeFormatter);
        } else if (daysBetween == 1) {
            return "Yesterday, " + dateTime.format(timeFormatter);
        } else if (daysBetween <= 6) {
            String dayName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            return dayName + ", " + dateTime.format(timeFormatter);
        } else {
            return dateTime.format(fullFormatter);
        }
    }
}
