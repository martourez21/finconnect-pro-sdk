package com.codedstreams.finconnectpro.sdk.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Utility class for cryptographic operations.
 * <p>
 * This class provides common cryptographic functions used for securing
 * sensitive data within the SDK, such as password encryption and data signing.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public final class CryptoUtils {

    private static final String AES_ALGORITHM = "AES";
    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Private constructor to prevent instantiation.
     */
    private CryptoUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Encrypts a string using AES encryption.
     *
     * @param data the data to encrypt
     * @param secretKey the secret key for encryption (must be 16, 24, or 32 bytes)
     * @return the Base64 encoded encrypted string
     * @throws CryptoException if encryption fails
     */
    public static String encrypt(String data, String secretKey) throws CryptoException {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {
            throw new CryptoException("Encryption failed", e);
        }
    }

    /**
     * Decrypts an AES encrypted string.
     *
     * @param encryptedData the Base64 encoded encrypted data
     * @param secretKey the secret key for decryption (must match encryption key)
     * @return the decrypted string
     * @throws CryptoException if decryption fails
     */
    public static String decrypt(String encryptedData, String secretKey) throws CryptoException {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new CryptoException("Decryption failed", e);
        }
    }

    /**
     * Generates a SHA-256 hash of the input data.
     *
     * @param data the data to hash
     * @return the hexadecimal representation of the hash
     * @throws CryptoException if hashing fails
     */
    public static String hash(String data) throws CryptoException {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new CryptoException("Hashing failed", e);
        }
    }

    /**
     * Masks sensitive data for logging purposes.
     *
     * @param data the sensitive data to mask
     * @param visibleChars the number of characters to keep visible at the end
     * @return the masked string
     */
    public static String maskSensitiveData(String data, int visibleChars) {
        if (data == null || data.length() <= visibleChars) {
            return "***"; // Return full mask if data is too short
        }

        int maskLength = data.length() - visibleChars;
        String maskedPart = "*".repeat(maskLength);
        String visiblePart = data.substring(maskLength);

        return maskedPart + visiblePart;
    }

    /**
     * Exception class for cryptographic operations.
     */
    public static class CryptoException extends Exception {
        public CryptoException(String message) {
            super(message);
        }

        public CryptoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
