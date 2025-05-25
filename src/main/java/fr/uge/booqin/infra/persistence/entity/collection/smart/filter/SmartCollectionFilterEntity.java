package fr.uge.booqin.infra.persistence.entity.collection.smart.filter;

import fr.uge.booqin.domain.model.collection.smart.filter.SmartCollectionFilterModel;
import fr.uge.booqin.infra.persistence.entity.collection.smart.SmartCollectionEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "smart_collection_filter")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class SmartCollectionFilterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "smart_collection_id")
    private SmartCollectionEntity smartCollection;

    public SmartCollectionEntity getSmartCollection() {
        return smartCollection;
    }

    public void setSmartCollection(SmartCollectionEntity smartCollection) {
        this.smartCollection = smartCollection;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public abstract SmartCollectionFilterModel convertTo();
}
