package fr.uge.booqin.app.service.loan.bookstock;

import fr.uge.booqin.app.service.loan.BookInfo;

import java.time.Instant;
import java.util.UUID;

public class BookLock {
            
    private final BookInfo bookInfo;
    private final Instant expirationTime;
    private final UUID token;

    BookLock(BookInfo bookInfo, Instant expirationTime, UUID token) {
        this.bookInfo = bookInfo;
        this.expirationTime = expirationTime;
        this.token = token;
    }

    public BookInfo bookInfo() {
        return bookInfo;
    }

    public Instant expirationTime() {
        return expirationTime;
    }

    public UUID token() {
        return token;
    }

    public UUID bookId() {
        return bookInfo.bookId();
    }
}