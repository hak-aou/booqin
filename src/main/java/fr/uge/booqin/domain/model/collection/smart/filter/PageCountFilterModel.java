package fr.uge.booqin.domain.model.collection.smart.filter;

import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.FilterLogic;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.PageCountFilterEntity;
import org.springframework.data.jpa.domain.Specification;


public record PageCountFilterModel(
        int number,
        FilterLogic logic
) implements SmartCollectionFilterModel {

    @Override
    public Specification<BookEntity> toSpecification() {
        return (root, query, builder) -> switch (logic) {
            case AFTER -> builder.greaterThan(root.get("pageCount"), number);
            case BEFORE -> builder.lessThan(root.get("pageCount"), number);
            case EQUAL -> builder.equal(root.get("pageCount"), number);
            case AFTER_OR_EQUAL -> builder.greaterThanOrEqualTo(root.get("pageCount"), number);
            case BEFORE_OR_EQUAL -> builder.lessThanOrEqualTo(root.get("pageCount"), number);
        };
    }

    @Override
    public PageCountFilterEntity convertTo() {
        return new PageCountFilterEntity(number, logic);
    }
}