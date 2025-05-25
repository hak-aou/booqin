package fr.uge.booqin.infra.persistence.entity.collection.smart.filter;

import fr.uge.booqin.domain.model.collection.smart.filter.TitleFilterModel;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "title_filters")
public class TitleFilterEntity extends SmartCollectionFilterEntity {
    private String title;

    public TitleFilterEntity() {
    }

    public TitleFilterEntity(String title) {
        this.title = Objects.requireNonNull(title, "title is required");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public TitleFilterModel convertTo() {
        return new TitleFilterModel(title);
    }
}
