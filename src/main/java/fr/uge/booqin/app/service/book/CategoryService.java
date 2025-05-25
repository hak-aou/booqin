package fr.uge.booqin.app.service.book;

import fr.uge.booqin.infra.persistence.entity.book.CategoryEntity;
import fr.uge.booqin.infra.persistence.repository.book.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryEntity findOrCreateCategory(String name) {
        var category = categoryRepository.findByCategoryName(name);
        if(category.isPresent()) {
            return category.get();
        }
        var categoryEntity = new CategoryEntity(name);
        categoryRepository.save(categoryEntity);
        return categoryEntity;
    }

    @Transactional
    public List<String> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryEntity::getCategoryName)
                .toList();
    }
}
