/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.util;

import java.util.regex.Pattern;

/**
 *
 * @author PhilipManteAsare
 */
public class ValidationUtil {
    private static final Pattern GHANA_CARD_PATTERN = Pattern.compile("^GHA-\\d{9}-\\d$");

    public static boolean isValidGhanaCardNumber(String ghanaCardNumber) {
        if (ghanaCardNumber == null) {
            return false;
        }
        return GHANA_CARD_PATTERN.matcher(ghanaCardNumber).matches();
    }
}
