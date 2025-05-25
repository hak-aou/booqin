package fr.uge.booqin.infra.persistence.entity.collection.smart.filter;

import fr.uge.booqin.domain.model.collection.smart.filter.HasSubtitleFilterModel;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "has_subtitle_filters")
public class HasSubtitleFilterEntity extends SmartCollectionFilterEntity {
    private Boolean hasSubtitle;

    public HasSubtitleFilterEntity() {
    }

    public HasSubtitleFilterEntity(Boolean hasSubtitle) {
        this.hasSubtitle = Objects.requireNonNull(hasSubtitle, "hasSubtitle is required");
    }

    public Boolean getHasSubtitle() {
        return hasSubtitle;
    }

    public void setHasSubtitle(Boolean hasSubtitle) {
        this.hasSubtitle = hasSubtitle;
    }

    @Override
    public HasSubtitleFilterModel convertTo() {
        return new HasSubtitleFilterModel(hasSubtitle);
    }
}
