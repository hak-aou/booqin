package fr.uge.booqin.infra.external.book.query_parameter;

import java.util.HashMap;

public class OpenLibraryQueryParameterBuilder implements QueryParameterBuilder{

    private static final String OPENLIBRARY_API_URL = "https://openlibrary.org/search.json";

    private final HashMap<String, String> queryParameters = new HashMap<>();

    @Override
    public QueryParameterBuilder language(QueryLanguage language) {
        queryParameters.put("language", language.getOpenLibraryFormat());
        return this;
    }

    @Override
    public QueryParameterBuilder title(String title) {
        queryParameters.put("title", title);
        return this;
    }

    @Override
    public QueryParameterBuilder author(String author) {
        queryParameters.put("author", author);
        return this;
    }

    @Override
    public QueryParameterBuilder publisher(String publisher) {
        queryParameters.put("publisher", publisher);
        return this;
    }

    @Override
    public QueryParameterBuilder isbn(String isbn) {
        queryParameters.put("isbn", isbn);
        return this;
    }

    @Override
    public QueryParameterBuilder offset(int offset) {
        queryParameters.put("offset", String.valueOf(offset));
        return this;
    }

    @Override
    public QueryParameterBuilder limit(int limit) {
        queryParameters.put("limit", String.valueOf(limit));
        return this;
    }

    @Override
    public String build() {
        StringBuilder stringBuilder = new StringBuilder();
        queryParameters.forEach((key, value) -> {
            var query = String.format("%s:%s&", key, value);
            stringBuilder.append(query);
        });
        return String.format("%s?q=%s", OPENLIBRARY_API_URL, stringBuilder);
    }

}
