package com.oscar.cardplatform.service.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PanUtils {

    private PanUtils() {}

    public static String mask(String pan) {
        if (pan == null || pan.length() < 10) {
            throw new IllegalArgumentException("PAN inválido");
        }
        return pan.substring(0, 4) + "********" + pan.substring(pan.length() - 4);
    }

    public static String hash(String pan) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(pan.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : encoded) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear PAN", e);
        }
    }
}
