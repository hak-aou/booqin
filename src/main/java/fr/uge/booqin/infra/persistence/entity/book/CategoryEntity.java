package fr.uge.booqin.infra.persistence.entity.book;

import fr.uge.booqin.domain.model.collection.smart.filter.CategoryFilterModel;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.SmartCollectionFilterEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class CategoryEntity extends SmartCollectionFilterEntity {

    @Column(name = "category_name", unique = true)
    private String categoryName;

    public CategoryEntity() {}

    public CategoryEntity(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public CategoryFilterModel convertTo() {
        return new CategoryFilterModel(categoryName);
    }
}
