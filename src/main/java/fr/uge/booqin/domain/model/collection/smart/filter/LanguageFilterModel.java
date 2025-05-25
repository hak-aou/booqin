package fr.uge.booqin.domain.model.collection.smart.filter;

import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.book.LanguageEntity;
import org.springframework.data.jpa.domain.Specification;

public record LanguageFilterModel(
        String language
) implements SmartCollectionFilterModel {

    @Override
    public Specification<BookEntity> toSpecification() {
        return (root, query, builder) -> builder.equal(root.get("language").get("languageName"), language);
    }

    @Override
    public LanguageEntity convertTo() {
        return new LanguageEntity(language);
    }
}
