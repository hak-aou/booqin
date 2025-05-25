package fr.uge.booqin.domain.model.rules.comment;


import java.util.regex.Pattern;

@FunctionalInterface
public interface CommentSanitizer {
    String sanitize(String comment);

    static CommentSanitizer useDefault() {

        var blackList = new String[] {"forax"};

        var pattern = Pattern.compile(String.join("|", blackList), Pattern.CASE_INSENSITIVE);

        return comment -> {
            var matcher = pattern.matcher(comment);
            return matcher.replaceAll("<he who must not be named>");
        };
    }
}
