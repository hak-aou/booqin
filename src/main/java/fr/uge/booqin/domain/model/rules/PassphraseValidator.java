package fr.uge.booqin.domain.model.rules;

import java.util.List;

@FunctionalInterface
public interface PassphraseValidator {
    boolean validate(List<String> passphrase);

    ///
    /// @param minLength required minimum length words in passphrase (inclusive)
    /// @param maxLength required maximum length words in passphrase (inclusive)
    ///
    static PassphraseValidator useDefault(int minLength, int maxLength) {
        if(minLength < 6) {
            throw new IllegalArgumentException("Minimum length must be at least 6");
        }
        return passphrase -> {
            if (passphrase == null) {
                return false;
            }
            if(passphrase.isEmpty()) {
                return false;
            }
            if(passphrase.size() < minLength || passphrase.size() > maxLength) {
                return false;
            }
            return passphrase.stream().noneMatch(String::isBlank);
        };
    }
}
