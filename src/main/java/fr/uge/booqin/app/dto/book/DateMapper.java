package fr.uge.booqin.app.dto.book;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

// https://stackoverflow.com/questions/4024544/how-to-parse-dates-in-multiple-formats-using-simpledateformat
public class DateMapper {
    static final List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"), // google api
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy") // openlibrary api
    );

    public static LocalDate convertTo(String stringDate) {
        for (var formatter : formatters) {
            try {
                return LocalDate.parse(stringDate, formatter);
            } catch (DateTimeParseException e) {
                // ignore
            }
        }
        return null;
    }
}
