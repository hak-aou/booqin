package fr.uge.booqin.infra.persistence.entity.collection.smart.filter;

import fr.uge.booqin.domain.model.collection.smart.filter.LanguageFilterModel;
import fr.uge.booqin.infra.persistence.entity.book.LanguageEntity;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "language_filters")
public class LanguageFilterEntity extends SmartCollectionFilterEntity {

    @ManyToOne
    private LanguageEntity language;

    public LanguageFilterEntity() {
    }

    public LanguageFilterEntity(LanguageEntity language) {
        this.language = Objects.requireNonNull(language, "language is required");
    }

    public LanguageEntity getLanguage() {
        return language;
    }

    public void setLanguage(LanguageEntity language) {
        this.language = language;
    }

    @Override
    public LanguageFilterModel convertTo() {
        return new LanguageFilterModel(language.getLanguageName());
    }
}
