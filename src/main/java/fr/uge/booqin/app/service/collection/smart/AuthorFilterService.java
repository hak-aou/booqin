package fr.uge.booqin.app.service.collection.smart;

import fr.uge.booqin.app.dto.filter.FilterBooksDTO;
import fr.uge.booqin.app.service.book.AuthorService;
import fr.uge.booqin.infra.persistence.entity.collection.smart.SmartCollectionEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.AuthorFilterEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.SmartCollectionFilterEntity;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthorFilterService implements FilterService {
    private final AuthorService authorService;

    public AuthorFilterService(AuthorService authorService) {
        this.authorService = authorService;
    }

    @Override
    public Set<SmartCollectionFilterEntity> createFilters(SmartCollectionEntity smartCollectionEntity, FilterBooksDTO filterBooksDTO) {
        if (filterBooksDTO.authors() == null || filterBooksDTO.authors().isEmpty()) {
            return Set.of();
        }

        return filterBooksDTO.languages().stream()
                .map(authorService::findOrCreateAuthor)
                .map(authorEntity -> {
                    var authorFilterEntity = new AuthorFilterEntity(authorEntity);
                    authorFilterEntity.setSmartCollection(smartCollectionEntity);
                    return authorFilterEntity;
                })
                .collect(Collectors.toSet());
    }
}
