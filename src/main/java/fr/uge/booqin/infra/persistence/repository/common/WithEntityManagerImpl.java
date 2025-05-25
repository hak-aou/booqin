package fr.uge.booqin.infra.persistence.repository.common;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;
import java.util.function.Function;

public class WithEntityManagerImpl implements WithEntityManager {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void inTransaction(Consumer<EntityManager> consumer) {
        consumer.accept(entityManager);
    }

    @Transactional
    public <T> T inTransaction(Function<EntityManager, T> action) {
        return action.apply(entityManager);
    }
}
