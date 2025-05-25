package fr.uge.booqin.infra.external.book.adapter;


import fr.uge.booqin.domain.model.books.BookModel;
import fr.uge.booqin.infra.external.book.query_parameter.QueryParameterBuilder;

import java.util.List;

public interface BookAPI {
    List<BookModel> getBookMetadata(QueryParameterBuilder queryParameterAPI);

}
