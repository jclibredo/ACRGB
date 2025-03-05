/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import java.security.SecureRandom;

/**
 *
 * @author DRG_SHADOWBILLING
 */
public class GenerateRandomPassword {

    public GenerateRandomPassword() {
    }

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = UPPER.toLowerCase();
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$";
    private static final String ALL_CHARS = UPPER + LOWER + DIGITS + SPECIAL_CHARS;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String GenerateRandomPassword(int length) {
        StringBuilder password = new StringBuilder(length);
        // At least one uppercase letter
        password.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        // At least one lowercase letter
        password.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        // At least one digit
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        // At least one special character
        password.append(SPECIAL_CHARS.charAt(RANDOM.nextInt(SPECIAL_CHARS.length())));

        // Remaining characters randomly selected from all characters
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }
        return password.toString();
    }

}
