/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.util;

import com.margins.STIM.entity.websocket.FingerCaptured;
import com.margins.STIM.model.CapturedFinger;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import static javax.print.attribute.ResolutionSyntax.DPI;
import org.apache.commons.codec.binary.Base64;
import org.primefaces.model.DefaultStreamedContent;

import org.primefaces.model.StreamedContent;


/**
 *
 * @author PhilipManteAsare
 */
public class Functions {
    
    private static final double INCH_2_CM = 2.54;

    public static byte[] processData(BufferedImage image) throws IOException {
        if (image == null) {
            throw new IllegalArgumentException("Error: BufferedImage is NULL!");
        }

        Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName("png");
        if (!iw.hasNext()) {
            throw new IOException("No PNG ImageWriter found!");
        }

        ImageWriter writer = iw.next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);

        if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
            throw new IOException("Metadata is read-only or unsupported.");
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(output)) {
            setDPI(metadata);
            writer.setOutput(stream);
            writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
        }

        return output.toByteArray();
    }

    
    public static void setDPI(IIOMetadata metadata) throws IIOInvalidTreeException {

        // for PMG, it's dots per millimeter
        double dotsPerMilli = 5.0 * DPI / 10 / 2.54;

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
    }
    
    public static StreamedContent createImageFromBytes(byte[] imageData, String mimeType) {
        if (imageData == null || mimeType == null) {
            return DefaultStreamedContent.builder().stream(() -> new ByteArrayInputStream(new byte[0])).build();
        }

        return DefaultStreamedContent.builder()
                .contentType(mimeType) // Example: "image/png"
                .stream(() -> new ByteArrayInputStream(imageData))
                .build();
    }
    public static BufferedImage createImageFromBytes(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
            public static StreamedContent createPhotoFromBytes(byte[] imageData) {
        if (imageData == null) {
            return DefaultStreamedContent.builder()
                    .stream(() -> new ByteArrayInputStream(new byte[0])) // Empty content
                    .contentType("image/png")
                    .build();
        }

        return DefaultStreamedContent.builder()
                .contentType("image/png")
                .stream(() -> new ByteArrayInputStream(imageData))
                .build();
    }

    public static void convertData(List<FingerCaptured> captured, CapturedFinger capturedFinger) throws IOException {

        for (FingerCaptured fc : captured) {
            if ("LI".equals(fc.getPosition())) {
                BufferedImage bi = createImageFromBytes(fc.getImage());
                String b64 = Base64.encodeBase64String(processData(bi));
                capturedFinger.setLeftIndex(b64);
            }

            if ("LM".equals(fc.getPosition())) {
                BufferedImage bi = createImageFromBytes(fc.getImage());
                String b64 = Base64.encodeBase64String(processData(bi));
                capturedFinger.setLeftMiddle(b64);
            }

            if ("LR".equals(fc.getPosition())) {
                BufferedImage bi = createImageFromBytes(fc.getImage());
                String b64 = Base64.encodeBase64String(processData(bi));
                capturedFinger.setLeftRing(b64);
            }

            if ("LL".equals(fc.getPosition())) {
                BufferedImage bi = createImageFromBytes(fc.getImage());
                String b64 = Base64.encodeBase64String(processData(bi));
                capturedFinger.setLeftIndex(b64);
            }

            if ("LI".equals(fc.getPosition())) {
                BufferedImage bi = createImageFromBytes(fc.getImage());
                String b64 = Base64.encodeBase64String(processData(bi));
                capturedFinger.setLeftLittle(b64);
            }

            if ("LT".equals(fc.getPosition())) {
                BufferedImage bi = createImageFromBytes(fc.getImage());
                String b64 = Base64.encodeBase64String(processData(bi));
                capturedFinger.setLeftThumb(b64);
            }

            if ("RT".equals(fc.getPosition())) {
                BufferedImage bi = createImageFromBytes(fc.getImage());
                String b64 = Base64.encodeBase64String(processData(bi));
                capturedFinger.setRightThumb(b64);
            }

            if ("RI".equals(fc.getPosition())) {
                BufferedImage bi = createImageFromBytes(fc.getImage());
                String b64 = Base64.encodeBase64String(processData(bi));
                capturedFinger.setRightIndex(b64);
            }

            if ("RM".equals(fc.getPosition())) {
                BufferedImage bi = createImageFromBytes(fc.getImage());
                String b64 = Base64.encodeBase64String(processData(bi));
                capturedFinger.setRightMiddle(b64);
            }

            if ("RL".equals(fc.getPosition())) {
                BufferedImage bi = createImageFromBytes(fc.getImage());
                String b64 = Base64.encodeBase64String(processData(bi));
                capturedFinger.setRightLittle(b64);
            }

            if ("RR".equals(fc.getPosition())) {
                BufferedImage bi = createImageFromBytes(fc.getImage());
                String b64 = Base64.encodeBase64String(processData(bi));
                capturedFinger.setRightRing(b64);
            }
        }
    }
    
    
    
}
