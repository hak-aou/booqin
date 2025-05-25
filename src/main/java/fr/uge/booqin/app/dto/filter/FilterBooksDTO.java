package fr.uge.booqin.app.dto.filter;

import fr.uge.booqin.infra.persistence.entity.book.AuthorEntity;
import fr.uge.booqin.infra.persistence.entity.book.CategoryEntity;
import fr.uge.booqin.infra.persistence.entity.book.LanguageEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record FilterBooksDTO(
        String title,
        Boolean hasSubtitle,
        List<String> categories,
        List<String> languages,
        List<String> authors,
        IntervalNumberDTO pageCountInterval,
        IntervalDateDTO publishedDateInterval
) implements FilterDTO {


    public Set<SmartCollectionFilterEntity> convertTo() {
        HashSet<SmartCollectionFilterEntity> filters = new HashSet<>();

        if (title != null && !title.isEmpty())
            filters.add(new TitleFilterEntity(title));

        if (hasSubtitle != null)
            filters.add(new HasSubtitleFilterEntity(hasSubtitle));

        if (categories != null && !categories.isEmpty()) {
            for (var category : categories)
                if(category != null && !category.isEmpty())
                    filters.add(new CategoryFilterEntity(new CategoryEntity(category)));
        }

        if (languages != null && !languages.isEmpty()) {
            for (var language : languages)
                if(language != null && !language.isEmpty())
                    filters.add(new LanguageFilterEntity(new LanguageEntity(language)));
        }


        if (authors != null && !authors.isEmpty()) {
            for (var author : authors) {
                if(author != null && !author.isEmpty())
                    filters.add(new AuthorFilterEntity(new AuthorEntity(author)));
            }
        }

        if (pageCountInterval != null) {
            if (pageCountInterval.min() > 0)
                filters.add(new PageCountFilterEntity(pageCountInterval.min(), FilterLogic.AFTER));
            if (pageCountInterval.max() > 0)
                filters.add(new PageCountFilterEntity(pageCountInterval.max(), FilterLogic.BEFORE));
        }

        if (publishedDateInterval != null) {
            if (publishedDateInterval.min() != null)
                filters.add(new DateFilterEntity(publishedDateInterval.min(), FilterLogic.AFTER));
            if (publishedDateInterval.max() != null)
                filters.add(new DateFilterEntity(publishedDateInterval.max(), FilterLogic.BEFORE));
        }

        return filters;
    }

}
