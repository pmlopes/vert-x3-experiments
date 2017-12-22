package xyz.jetdrone.blockchain.impl;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hash {

    private static final MessageDigest sha256;
    private final static char[] HEX = "0123456789abcdef".toCharArray();
    private static final Charset UTF8 = Charset.forName("UTF8");

    static {
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private Hash() {
        throw new RuntimeException("Hash is not instantiable");
    }

    public static String sha256(byte[] data) {
        byte[] bytes;
        synchronized (sha256) {
            bytes = sha256.digest(data);
        }
        return bytesToHex(bytes);
    }

    public static String sha256(String data) {
        return sha256(data.getBytes(UTF8));
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX[v >>> 4];
            hexChars[j * 2 + 1] = HEX[v & 0x0F];
        }
        return new String(hexChars);
    }

}
