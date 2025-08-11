/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.util;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author PhilipManteAsare
 */
public class ValidationUtil {
    private static Pattern reg = Pattern.compile("[A-Z]{3}-\\d{9}-(-?)\\d");
    private static Pattern regEmail = Pattern.compile("[\\w._%+-]+@.+.\\w{2,5}");
    /// gha-726772889--7

    public static boolean isValidGhanaCardNumber(String ghanaCardNumber) {
        if (ghanaCardNumber == null) {
            return false;
        }
        return reg.matcher(ghanaCardNumber).matches();
    }
    
    public static String beautifyEnumName(Enum<?> e) {
        String name = e.toString().toLowerCase().replace('_', ' ');
        return Arrays.stream(name.split(" "))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
