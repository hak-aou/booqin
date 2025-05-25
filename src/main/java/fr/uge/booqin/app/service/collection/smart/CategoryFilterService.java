package fr.uge.booqin.app.service.collection.smart;

import fr.uge.booqin.app.dto.filter.FilterBooksDTO;
import fr.uge.booqin.app.service.book.CategoryService;
import fr.uge.booqin.infra.persistence.entity.collection.smart.SmartCollectionEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.CategoryFilterEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.SmartCollectionFilterEntity;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryFilterService implements FilterService {
    private final CategoryService categoryService;

    public CategoryFilterService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public Set<SmartCollectionFilterEntity> createFilters(SmartCollectionEntity smartCollectionEntity, FilterBooksDTO filterBooksDTO) {
        if (filterBooksDTO.categories() == null || filterBooksDTO.categories().isEmpty()) {
            return Set.of();
        }

        return filterBooksDTO.categories().stream()
                .map(categoryService::findOrCreateCategory)
                .map(categoryEntity -> {
                    var filterEntity = new CategoryFilterEntity(categoryEntity);
                    filterEntity.setSmartCollection(smartCollectionEntity);
                    return filterEntity;
                })
                .collect(Collectors.toSet());
    }
}
