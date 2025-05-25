package fr.uge.booqin.infra.persistence.entity.collection.smart.filter;

import fr.uge.booqin.domain.model.collection.smart.filter.CategoryFilterModel;
import fr.uge.booqin.infra.persistence.entity.book.CategoryEntity;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "category_filters")
public class CategoryFilterEntity extends SmartCollectionFilterEntity {

    @ManyToOne
    private CategoryEntity category;

    public CategoryFilterEntity() {
    }

    public CategoryFilterEntity(CategoryEntity category) {
        this.category = Objects.requireNonNull(category, "category is required");
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    @Override
    public CategoryFilterModel convertTo() {
        return new CategoryFilterModel(category.getCategoryName());
    }
}
