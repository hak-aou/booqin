package fr.uge.booqin.infra.persistence.repository.book;

import fr.uge.booqin.infra.persistence.entity.book.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    @Query("SELECT c FROM CategoryEntity c WHERE c.categoryName = :name")
    Optional<CategoryEntity> findByCategoryName(String name);
}
