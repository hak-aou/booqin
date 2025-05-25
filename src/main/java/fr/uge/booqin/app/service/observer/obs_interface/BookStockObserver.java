package fr.uge.booqin.app.service.observer.obs_interface;

import fr.uge.booqin.app.service.loan.BookInfo;

import java.util.UUID;

public interface BookStockObserver {
    default void bookAvailable(BookInfo bookInfo) {}
    default void expiredLockToken(UUID bookId, UUID token) {}
}
