package com.vcore.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Utility class for computing MD5 hash digests of strings.
 * Produces lowercase hexadecimal MD5 strings using UTF-8 encoding for input conversion.
 */
public class Md5Utils {
    /** Hex digit lookup table for fast byte-to-hex conversion. */
    private static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Computes the MD5 hash of the given input string, returning it as a 32-character
     * lowercase hexadecimal string.
     *
     * @param input the string to hash; if {@code null}, returns {@code null}
     * @return the 32-character lowercase hex MD5 hash, or {@code null} if the input is
     *         {@code null} or the MD5 algorithm is unavailable
     */
    public static String md5(String input) {
        if (input == null) {
            return null;
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] inputByteArray = input.getBytes(StandardCharsets.UTF_8);
            messageDigest.update(inputByteArray);

            byte[] resultByteArray = messageDigest.digest();
            return byteArrayToHex(resultByteArray);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Converts a byte array to a lowercase hexadecimal string.
     * Each byte is converted to exactly two hex characters.
     *
     * @param byteArray the byte array to convert
     * @return a lowercase hex string of length {@code byteArray.length * 2}
     */
    private static String byteArrayToHex(byte[] byteArray) {
        char[] resultCharArray = new char[byteArray.length * 2];
        int index = 0;

        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        return new String(resultCharArray);
    }
}
