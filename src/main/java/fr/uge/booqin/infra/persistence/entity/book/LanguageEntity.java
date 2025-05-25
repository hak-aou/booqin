package fr.uge.booqin.infra.persistence.entity.book;

import fr.uge.booqin.domain.model.collection.smart.filter.LanguageFilterModel;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.SmartCollectionFilterEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "languages")
public class LanguageEntity extends SmartCollectionFilterEntity {

    @Column(name = "name", unique = true)
    private String languageName;

    public LanguageEntity() {
    }

    public LanguageEntity(String languageName) {
        this.languageName = languageName;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    @Override
    public LanguageFilterModel convertTo() {
        return new LanguageFilterModel(languageName);
    }
}


