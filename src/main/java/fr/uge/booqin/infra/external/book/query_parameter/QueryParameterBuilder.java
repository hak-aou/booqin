package fr.uge.booqin.infra.external.book.query_parameter;

public interface QueryParameterBuilder {
    QueryParameterBuilder language(QueryLanguage language);
    QueryParameterBuilder title(String title);
    QueryParameterBuilder author(String author);
    QueryParameterBuilder publisher(String publisher);
    QueryParameterBuilder isbn(String isbn);
    QueryParameterBuilder offset(int offset);
    QueryParameterBuilder limit(int limit);
    // QueryParameterBuilder type(BookFormat type);

    String build();
}
