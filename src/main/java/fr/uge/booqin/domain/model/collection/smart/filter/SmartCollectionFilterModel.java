package fr.uge.booqin.domain.model.collection.smart.filter;

import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.SmartCollectionFilterEntity;
import org.springframework.data.jpa.domain.Specification;

public interface SmartCollectionFilterModel {
    Specification<BookEntity> toSpecification();

    SmartCollectionFilterEntity convertTo();
}
