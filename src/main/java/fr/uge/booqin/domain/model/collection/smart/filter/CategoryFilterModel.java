package fr.uge.booqin.domain.model.collection.smart.filter;

import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.book.CategoryEntity;
import org.springframework.data.jpa.domain.Specification;

// https://stackoverflow.com/questions/47714537/spring-data-specification-for-set-contains-operation

public record CategoryFilterModel(
        String categoryName
) implements SmartCollectionFilterModel {

    @Override
    public Specification<BookEntity> toSpecification() {
        return (root, query, builder) -> {
            var categories = root.join("categories");
            return builder.equal(categories.get("categoryName"), categoryName);
        };
    }

    @Override
    public CategoryEntity convertTo() {
        return new CategoryEntity(categoryName);
    }
}
