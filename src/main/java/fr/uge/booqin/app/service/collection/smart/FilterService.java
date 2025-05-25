package fr.uge.booqin.app.service.collection.smart;

import fr.uge.booqin.app.dto.filter.FilterBooksDTO;
import fr.uge.booqin.infra.persistence.entity.collection.smart.SmartCollectionEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.SmartCollectionFilterEntity;

import java.util.Set;

public interface FilterService {
    Set<SmartCollectionFilterEntity> createFilters(SmartCollectionEntity smartCollectionEntity,
                                                   FilterBooksDTO filterBooksDTO);
}
