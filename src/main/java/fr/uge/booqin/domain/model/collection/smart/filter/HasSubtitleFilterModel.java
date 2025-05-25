package fr.uge.booqin.domain.model.collection.smart.filter;

import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.HasSubtitleFilterEntity;
import org.springframework.data.jpa.domain.Specification;


public record HasSubtitleFilterModel(
        Boolean hasSubTitle
) implements SmartCollectionFilterModel {

    @Override
    public Specification<BookEntity> toSpecification() {
        return (root, query, builder) -> {
            if (hasSubTitle) {
                return builder.isNotNull(root.get("subtitle"));
            }
            return builder.isNull(root.get("subtitle"));
        };
    }

    @Override
    public HasSubtitleFilterEntity convertTo() {
        return new HasSubtitleFilterEntity(hasSubTitle);
    }
}