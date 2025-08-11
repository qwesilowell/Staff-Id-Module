/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

/**
 *
 * @author PhilipManteAsare
 */
public class Util {
        public static InputStream toInputStream(String base64) {
        // Decode the Base64 string into a byte array
        byte[] imageBytes = Base64.getDecoder().decode(base64);

        // Convert the byte array to an InputStream
        InputStream inputStream = new ByteArrayInputStream(imageBytes);

        return inputStream;
    }
}
