package fr.uge.booqin.app.service.collection.smart;

import fr.uge.booqin.app.dto.filter.FilterBooksDTO;
import fr.uge.booqin.app.service.book.LanguageService;
import fr.uge.booqin.infra.persistence.entity.collection.smart.SmartCollectionEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.LanguageFilterEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.SmartCollectionFilterEntity;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LanguageFilterService implements FilterService {
    private final LanguageService languageService;

    public LanguageFilterService(LanguageService languageService) {
        this.languageService = languageService;
    }

    @Override
    public Set<SmartCollectionFilterEntity> createFilters(SmartCollectionEntity smartCollectionEntity, FilterBooksDTO filterBooksDTO) {
        if (filterBooksDTO.languages() == null || filterBooksDTO.languages().isEmpty()) {
            return Set.of();
        }

        return filterBooksDTO.languages().stream()
                .map(languageService::findOrCreateLanguage)
                .map(languageEntity -> {
                    var languageFilterEntity = new LanguageFilterEntity(languageEntity);
                    languageFilterEntity.setSmartCollection(smartCollectionEntity);
                    return languageFilterEntity;
                })
                .collect(Collectors.toSet());
    }

}
