/*
* Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
* Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
*/
package com.margins.STIM.util;
 
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.ImageTypeSpecifier;
//import javax.imageio.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Base64;
import javax.imageio.IIOImage;
 
/**
*
* @author BryanAnyanful
*/
public class FingerprintProcessor {
 
    // Method to convert byte array to BufferedImage
    public static BufferedImage createImageFromBytes(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException("Error reading image data", e);
        }
    }
 
    // Method to process the image and set DPI
    public static byte[] processData(BufferedImage image) throws IOException {
        for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName("png"); iw.hasNext();) {
            ImageWriter writer = iw.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
 
            // If the metadata is not writable, skip it
            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                continue;
            }
 
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageOutputStream stream = null;
            try {
                setDPI(metadata);  // Set the DPI in the metadata
                stream = ImageIO.createImageOutputStream(output);
                writer.setOutput(stream);
                writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            return output.toByteArray();  // Return processed image bytes
        }
        return null;  // If no writer was found, return null
    }
 
    public static void setDPI(IIOMetadata metadata) {
        try {
            // Define the DPI to be 100, as mentioned
            int dpi = 100;
            double dotsPerMilli = 5.0 * dpi / 10 / 2.54;
 
            IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
            horiz.setAttribute("value", Double.toString(dotsPerMilli));
 
            IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
            vert.setAttribute("value", Double.toString(dotsPerMilli));
 
            IIOMetadataNode dim = new IIOMetadataNode("Dimension");
            dim.appendChild(horiz);
            dim.appendChild(vert);
 
            IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
            root.appendChild(dim);
 
            metadata.mergeTree("javax_imageio_1.0", root);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the invalid tree case here (e.g., log an error or rethrow with additional context)
        }
    }
 
    public static String imageDpi (String base64String) {
        // Decode Base64 string to byte array
        byte[] fingerPrintData = Base64.getDecoder().decode(base64String);
 
        // Create a BufferedImage from the byte array
        BufferedImage bi = createImageFromBytes(fingerPrintData);
 
        try {
            // Process the image (set DPI)
            byte[] processedData = processData(bi);
            if (processedData != null) {
                // Convert processed image back to Base64
                String b64 = Base64.getEncoder().encodeToString(processedData);
//                System.out.println("Base64 Encoded Processed Image: " + b64);
                return b64;
            } else {
                System.out.println("Image processing failed.");
                return null;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Base64 input string: " + e.getMessage());
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}