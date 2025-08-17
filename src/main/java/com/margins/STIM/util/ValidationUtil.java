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
  private static final String EMAIL_REG = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}";
    private static final String GHANA_CARD_REG = "^[A-Z]{3}-\\d{8}[\\d-]-[\\dA-Z]$";
    private static final String DECIMAL_REG = "^-?\\d+(\\.\\d+)?$";


    public static boolean isDecimal(String value) {
        return value != null && Pattern.matches(DECIMAL_REG, value);
    }

    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isNull(Object value) {
        return value == null;
    }

    public static boolean isValidGhanaCardNumber(String pin) {

        if (isNullOrBlank(pin)) {
            return false;
        }
        return Pattern.compile(GHANA_CARD_REG)
                .matcher(pin)
                .matches();
    }
 
    public static boolean isValidEmail(String email) {
        if (isNullOrBlank(email)) {
            return true;
        }
        return Pattern.compile(EMAIL_REG, Pattern.CASE_INSENSITIVE)
                .matcher(email)
                .matches();
    }

    
    public static String validatePassword(String password) {
        if (!PasswordValidator.validatePasswordLength(password)) {
            return "Password must contain at least 8 characters";
        }

        if (!PasswordValidator.containsLowerCase(password)) {
            return "Password must contain a lowercase character";
        }

        if (!PasswordValidator.containsUpperCase(password)) {
            return "Password must contain an uppercase character";
        }

        if (!PasswordValidator.containsNumber(password)) {
            return "Password must contain a number";
        }

        if (!PasswordValidator.containsSpecialCharacter(password)) {
            return "Password must contain a special character";
        }

        return null;
    }


    public static boolean validatePhone(String phone) {

        if (phone == null || phone.isBlank()) {

            return false; // Null values are considered non-matching.

        }

        return phone.matches("^(\\+233|233|0)[0-9]{9}$");

    }

    public static String beautifyEnumName(Enum<?> e) {
        String name = e.toString().toLowerCase().replace('_', ' ');
        return Arrays.stream(name.split(" "))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}


 
