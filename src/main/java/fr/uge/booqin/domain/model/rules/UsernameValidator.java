package fr.uge.booqin.domain.model.rules;

import java.util.List;
import java.util.regex.Pattern;

@FunctionalInterface
public interface UsernameValidator {
    List<String> blacklist = List.of(
            "BooqIn",
            "booqin",
            "BOOQIN",
            "booqIn",
            "booqIN",
            "admin",
            "administrator",
            "test",
            "debug",
            "root",
            "moderator"
    );

    boolean validate(String username);

    ///
    /// A username validator that uses the default rules:
    /// - Username must not be null
    /// - Username must not be empty
    /// - Username must not be blank
    /// - Username should be at least 4 characters long
    /// - Username must not be longer than 50 characters
    /// - Username must not contain any special characters except for _ and -
    /// - Username must not start with a special character
    /// - Username must not end with a special character
    /// - Username must not contain two special characters in a row
    /// - Username must not contain spaces
    /// - Username shouldn't be in the blacklist
    static UsernameValidator useDefault() {
        var regex = "^(?=.{4,50}$)(?![_-])(?!.*[_-]{2})[A-Za-z0-9_-]+(?<![_-])$";
        var pattern = Pattern.compile(regex);
        return username -> username != null
                && !blacklist.contains(username)
                && pattern.matcher(username.trim()).matches();
    }
}
