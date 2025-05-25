package fr.uge.booqin.domain.model.collection.smart.filter;

import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.TitleFilterEntity;
import org.springframework.data.jpa.domain.Specification;


public record TitleFilterModel(
        String title
) implements SmartCollectionFilterModel {

    @Override
    public Specification<BookEntity> toSpecification() {
        return (root, query, builder) -> builder.like(root.get("title"), "%" + title + "%");
    }

    @Override
    public TitleFilterEntity convertTo() {
        return new TitleFilterEntity(title);
    }
}