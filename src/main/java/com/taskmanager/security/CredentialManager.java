package com.taskmanager.security;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Properties;

/**
 * Handles all security concerns using only built-in Java crypto:
 *
 *  • Password hashing  — PBKDF2WithHmacSHA256  (100 000 iterations, 256-bit key)
 *  • Config encryption — AES-256-GCM authenticated encryption
 *
 * No external libraries required.
 */
public class CredentialManager {

    private static final String CONFIG_DIR  = System.getProperty("user.home") + "/.taskmanager";
    private static final String CONFIG_FILE = CONFIG_DIR + "/config.enc";

    // PBKDF2 parameters
    private static final int    PBKDF2_ITERATIONS = 100_000;
    private static final int    HASH_BYTES        = 32;           // 256 bits
    private static final int    SALT_BYTES        = 16;

    // AES-GCM parameters
    private static final int    GCM_IV_LEN        = 12;           // 96 bits
    private static final int    GCM_TAG_LEN       = 128;          // bits

    // Master seed for config encryption (machine + app specific)
    private static final String MASTER_SEED =
        System.getProperty("user.name", "user") + "-taskmanager-2024-key-v1";

    // ─── Password Hashing (PBKDF2) ─────────────────────────────────────────

    /**
     * Hash a plain-text password.
     * Format: Base64(salt) : Base64(hash)
     */
    public static String hashPassword(String plain) {
        try {
            byte[] salt = randomBytes(SALT_BYTES);
            byte[] hash = pbkdf2(plain.toCharArray(), salt);
            return encode(salt) + ":" + encode(hash);
        } catch (Exception e) {
            throw new SecurityException("Hashing failed", e);
        }
    }

    /**
     * Verify a plain-text password against a stored hash.
     */
    public static boolean verifyPassword(String plain, String stored) {
        if (stored == null || !stored.contains(":")) return false;
        try {
            String[] parts = stored.split(":", 2);
            byte[] salt     = decode(parts[0]);
            byte[] expected = decode(parts[1]);
            byte[] actual   = pbkdf2(plain.toCharArray(), salt);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Config Encryption (AES-256-GCM) ───────────────────────────────────

    public static void saveConfig(String key, String value) throws IOException {
        Properties props = loadRawConfig();
        props.setProperty(key, encryptAES(value));
        writeConfig(props);
    }

    public static String loadConfig(String key) {
        Properties props = loadRawConfig();
        String enc = props.getProperty(key);
        if (enc == null) return null;
        try { return decryptAES(enc); } catch (Exception e) { return null; }
    }

    public static String encrypt(String plain) { return encryptAES(plain); }
    public static String decrypt(String cipher) {
        try { return decryptAES(cipher); } catch (Exception e) { return null; }
    }

    // ─── AES-256-GCM internals ──────────────────────────────────────────────

    private static String encryptAES(String plain) {
        try {
            SecretKey key = deriveAESKey();
            byte[] iv      = randomBytes(GCM_IV_LEN);
            Cipher cipher  = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key,
                new GCMParameterSpec(GCM_TAG_LEN, iv));
            byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            // Store: iv + ciphertext as Base64
            byte[] combined = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ct, 0, combined, iv.length, ct.length);
            return encode(combined);
        } catch (Exception e) {
            throw new SecurityException("Encryption failed", e);
        }
    }

    private static String decryptAES(String encoded) throws Exception {
        byte[] combined = decode(encoded);
        byte[] iv = new byte[GCM_IV_LEN];
        byte[] ct = new byte[combined.length - GCM_IV_LEN];
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LEN);
        System.arraycopy(combined, GCM_IV_LEN, ct, 0, ct.length);

        SecretKey key = deriveAESKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LEN, iv));
        return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
    }

    private static SecretKey deriveAESKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Derive a stable 256-bit AES key from the master seed using PBKDF2
        byte[] salt = "taskmanager-cfg-salt-v1".getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(pbkdf2WithLen(MASTER_SEED.toCharArray(), salt, 32), "AES");
    }

    // ─── PBKDF2 helpers ─────────────────────────────────────────────────────

    private static byte[] pbkdf2(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        return pbkdf2WithLen(password, salt, HASH_BYTES);
    }

    private static byte[] pbkdf2WithLen(char[] password, byte[] salt, int keyLen)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, keyLen * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        spec.clearPassword();
        return hash;
    }

    // ─── Utility ────────────────────────────────────────────────────────────

    private static byte[] randomBytes(int len) {
        byte[] b = new byte[len];
        new SecureRandom().nextBytes(b);
        return b;
    }

    private static String encode(byte[] b) { return Base64.getEncoder().encodeToString(b); }
    private static byte[] decode(String s) { return Base64.getDecoder().decode(s); }

    // ─── Config file I/O ────────────────────────────────────────────────────

    private static Properties loadRawConfig() {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return props;
        try (InputStream is = new FileInputStream(file)) {
            props.load(is);
        } catch (IOException ignored) {}
        return props;
    }

    private static void writeConfig(Properties props) throws IOException {
        Files.createDirectories(Paths.get(CONFIG_DIR));
        try (OutputStream os = new FileOutputStream(CONFIG_FILE)) {
            props.store(os, "TaskManager Encrypted Config — do not edit manually");
        }
        File file = new File(CONFIG_FILE);
        file.setReadable(false, false);
        file.setReadable(true, true);
        file.setWritable(false, false);
        file.setWritable(true, true);
    }
}
