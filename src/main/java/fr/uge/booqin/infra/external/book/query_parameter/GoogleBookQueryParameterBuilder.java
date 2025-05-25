package fr.uge.booqin.infra.external.book.query_parameter;

import java.util.HashMap;

public class GoogleBookQueryParameterBuilder implements QueryParameterBuilder{

    private static final String GOOGLEBOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes";
    private final HashMap<String, String> queryParameters = new HashMap<>();

    @Override
    public QueryParameterBuilder language(QueryLanguage language) {
        queryParameters.put("langRestrict=", language.getGoogleFormat());
        return this;
    }

    @Override
    public QueryParameterBuilder title(String title) {
        queryParameters.put("intitle=", title);
        return this;
    }

    @Override
    public QueryParameterBuilder author(String author) {
        queryParameters.put("inauthor:", "\"" + author + "\"");
        return this;
    }

    @Override
    public QueryParameterBuilder publisher(String publisher) {
        queryParameters.put("inpublisher=", publisher);
        return this;
    }

    @Override
    public QueryParameterBuilder isbn(String isbn) {
        queryParameters.put("isbn:", isbn);
        return this;
    }

    @Override
    public QueryParameterBuilder offset(int offset) {
        queryParameters.put("startIndex=", String.valueOf(offset));
        return this;
    }

    @Override
    public QueryParameterBuilder limit(int limit) {
        queryParameters.put("maxResults=", String.valueOf(limit));
        return this;
    }

    @Override
    public String build() {
        StringBuilder stringBuilder = new StringBuilder();
        queryParameters.forEach((key, value) -> {
            var query = String.format("%s%s&", key, value);
            stringBuilder.append(query);
        });
        return String.format("%s?q=%s", GOOGLEBOOKS_API_URL, stringBuilder);
    }

}
