package com.coop.payments.util;


public final class MaskUtil {
    private MaskUtil() { }

    public static String mask(String value) {
        if (value == null || value.length() <= 6) return "****";
        return value.substring(0, 3) + "****" + value.substring(value.length() - 3);
    }
}
