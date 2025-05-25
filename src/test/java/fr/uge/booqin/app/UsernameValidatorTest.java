package fr.uge.booqin.app;

import fr.uge.booqin.domain.model.rules.UsernameValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UsernameValidatorTest {

    private final UsernameValidator passwordValidator = UsernameValidator.useDefault();

    @Test
    public void validate() {
        assertFalse(passwordValidator.validate("test"));
        assertTrue(passwordValidator.validate("username123"));
        assertFalse(passwordValidator.validate("username123456789012340567890123456789012345678901234567890123456789"));
        assertTrue(passwordValidator.validate("user"));
        assertTrue(passwordValidator.validate("user123"));
        assertTrue(passwordValidator.validate("aze-azea-aze"));
        assertFalse(passwordValidator.validate("aze-azea-"));
        assertFalse(passwordValidator.validate("-aze-azea"));
        assertFalse(passwordValidator.validate("aze azea"));
        assertFalse(passwordValidator.validate("aze azea !"));
        assertFalse(passwordValidator.validate("admin"));
        assertFalse(passwordValidator.validate("administrator"));
        assertFalse(passwordValidator.validate("test"));
        assertFalse(passwordValidator.validate("debug"));
        assertFalse(passwordValidator.validate("root"));
        assertFalse(passwordValidator.validate("moderator"));
    }
}
