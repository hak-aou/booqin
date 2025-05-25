package fr.uge.booqin.infra.persistence.repository.common;

import jakarta.persistence.EntityManager;

import java.util.function.Consumer;
import java.util.function.Function;

public interface WithEntityManager {
    void inTransaction(Consumer<EntityManager> consumer);
    <T> T inTransaction(Function<EntityManager,T> action);
}
