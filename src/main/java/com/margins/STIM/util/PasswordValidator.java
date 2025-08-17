/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.margins.STIM.util;

import java.util.function.IntPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Ernest
 */
public class PasswordValidator {

    private final Pattern pattern;

    private static final Pattern p = Pattern.compile("[^A-Za-z0-9 ]");

    private static final String PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[@#$%!]).{8,40})";

    public PasswordValidator() {
        pattern = Pattern.compile(PASSWORD_PATTERN);
    }

    public boolean validate(final String password) {

        Matcher matcher = pattern.matcher(password);
        return matcher.matches();

    }

    public static boolean containsSpecialCharacter(String value) {
        Matcher matcher = p.matcher(value);
        return matcher.find();
    }

    public static boolean validatePasswordLength(String Password) {
        return Password.length() >= 8;
    }

    public static boolean containsLowerCase(String value) {
        return contains(value, i -> Character.isLetter(i) && Character.isLowerCase(i));
    }

    public static boolean containsUpperCase(String value) {
        return contains(value, i -> Character.isLetter(i) && Character.isUpperCase(i));
    }

    public static boolean containsNumber(String value) {
        return contains(value, Character::isDigit);
    }

    private static boolean contains(String value, IntPredicate predicate) {
        return value.chars().anyMatch(predicate);
    }
}
