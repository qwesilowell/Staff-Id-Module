/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author PhilipManteAsare
 */
public class EmailTemplateLoader {

    public static String loadTemplate(String path) throws Exception {
        String resourcePath = path.replace("src/main/resources/", "");

        try (InputStream is = EmailTemplateLoader.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Template not found: " + resourcePath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
