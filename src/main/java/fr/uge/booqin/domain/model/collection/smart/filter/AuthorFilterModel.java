package fr.uge.booqin.domain.model.collection.smart.filter;

import fr.uge.booqin.infra.persistence.entity.book.AuthorEntity;
import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.AuthorFilterEntity;
import org.springframework.data.jpa.domain.Specification;


public record AuthorFilterModel(
        String authorName
) implements SmartCollectionFilterModel {

    @Override
    public Specification<BookEntity> toSpecification() {
        return (root, query, builder) -> {
            var categories = root.join("authors");
            return builder.like(categories.get("name"), "%" + authorName + "%");
        };
    }

    @Override
    public AuthorFilterEntity convertTo() {
        return new AuthorFilterEntity(new AuthorEntity(authorName));
    }
}