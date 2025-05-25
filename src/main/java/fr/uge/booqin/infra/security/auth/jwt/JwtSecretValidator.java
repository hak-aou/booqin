package fr.uge.booqin.infra.security.auth.jwt;


public interface JwtSecretValidator {

    // Check if the secret is strong enough = 256 bits (32 characters)
    default boolean isSecretStrong(String secret) {
        if (secret == null || secret.length() < 32) {
            return false;  // Minimum length of 32 characters
        }

        // Check for at least one uppercase, one lowercase, one digit, and one special character
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        
        for (char c : secret.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpperCase = true;
            if (Character.isLowerCase(c)) hasLowerCase = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (!Character.isLetterOrDigit(c)) hasSpecialChar = true;
        }

        // All conditions should be true for the secret to be considered strong
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }
}
