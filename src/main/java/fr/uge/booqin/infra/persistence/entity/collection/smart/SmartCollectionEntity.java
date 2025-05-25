package fr.uge.booqin.infra.persistence.entity.collection.smart;

import fr.uge.booqin.app.dto.filter.FilterBooksDTO;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.SmartCollectionFilterEntity;
import fr.uge.booqin.infra.persistence.entity.collection.standard.CollectionEntity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "smart_collection")
public class SmartCollectionEntity extends CollectionEntity {

    @OneToMany(mappedBy = "smartCollection",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<SmartCollectionFilterEntity> filters = new HashSet<>();

    @Transient
    private FilterBooksDTO filterBooksDTO;

    public SmartCollectionEntity(String title,
                                 String description,
                                 boolean visibility,
                                 UserEntity user,
                                 Set<SmartCollectionFilterEntity> filters) {
        super(title, description, visibility, user);
        this.filters = Objects.requireNonNull(filters);
    }

    public SmartCollectionEntity() {
        super();
    }


    public FilterBooksDTO getFilterBooksDTO() {
        return filterBooksDTO;
    }

    public void setFilterBooksDTO(FilterBooksDTO filterBooksDTO) {
        this.filterBooksDTO = filterBooksDTO;
    }

    public Set<SmartCollectionFilterEntity> getFilters() {
        return filters;
    }

    public void setFilters(Set<SmartCollectionFilterEntity> filters) {
        this.filters = filters;
    }

}
