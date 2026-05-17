package com.cureshare.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtil — BCrypt password hashing.
 *
 * Usage:
 *   String hash = PasswordUtil.hash("mypassword");   // store this in DB
 *   boolean ok  = PasswordUtil.verify("mypassword", hash); // on login
 */
public class PasswordUtil {

    private static final int WORK_FACTOR = 12; // good balance of speed vs security

    /** Hash a plain-text password. Returns a 60-char BCrypt string. */
    public static String hash(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(WORK_FACTOR));
    }

    /** Verify a plain-text password against a stored BCrypt hash. */
    public static boolean verify(String plainText, String hash) {
        if (plainText == null || hash == null) return false;
        try {
            return BCrypt.checkpw(plainText, hash);
        } catch (Exception e) {
            return false;
        }
    }
}
