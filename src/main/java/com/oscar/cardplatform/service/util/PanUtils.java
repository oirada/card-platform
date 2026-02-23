package com.oscar.cardplatform.service.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

public class PanUtils {

    private PanUtils() {}

    public static String mask(String pan) {
        if (pan == null || pan.length() < 10) {
            throw new IllegalArgumentException("PAN inválido");
        }
        int len = pan.length();
        String start = pan.substring(0, Math.min(6, len));
        String end = pan.substring(Math.max(0, len - 4));
        StringBuilder middle = new StringBuilder();
        int hidden = Math.max(0, len - 10); // total oculto entre 6 y 4
        for (int i = 0; i < hidden; i++) middle.append('*');
        return start + middle + end;
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

    public static String identificador(String pan) {
        String fecha = LocalDate.now().toString();
        return hash(pan + "|" + fecha);
    }
}
