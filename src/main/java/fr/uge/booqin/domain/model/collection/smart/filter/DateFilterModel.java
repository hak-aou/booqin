package fr.uge.booqin.domain.model.collection.smart.filter;

import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.DateFilterEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.FilterLogic;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;


public record DateFilterModel(
        LocalDate date,
        FilterLogic logic
) implements SmartCollectionFilterModel {

    @Override
    public Specification<BookEntity> toSpecification() {
        return (root, query, builder) -> switch (logic) {
            case AFTER -> builder.greaterThan(root.get("publishedDate"), date);
            case BEFORE -> builder.lessThan(root.get("publishedDate"), date);
            case EQUAL -> builder.equal(root.get("publishedDate"), date);
            case AFTER_OR_EQUAL -> builder.greaterThanOrEqualTo(root.get("publishedDate"), date);
            case BEFORE_OR_EQUAL -> builder.lessThanOrEqualTo(root.get("publishedDate"), date);
        };
    }


    @Override
    public DateFilterEntity convertTo() {
        return new DateFilterEntity(date, logic);
    }
}