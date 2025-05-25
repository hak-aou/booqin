package fr.uge.booqin.infra.external.book.dto.deserialization.openlibrary.book;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

// https://www.baeldung.com/java-jackson-deserialize-particular-type
/// Desfois la description est un string et d'autres fois c'est un objet avec type et value.
/// ce code permet de gérer les deux cas
public class DescriptionDeserializer extends JsonDeserializer<Object> {
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken currentToken = p.getCurrentToken();

        if (currentToken == JsonToken.VALUE_STRING) {
            return p.getText();
        }
        return ctxt.readValue(p, OpenLibraryBook.Description.class).value();
    }
}
