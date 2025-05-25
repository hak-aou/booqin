package fr.uge.booqin.app.service.loan.bookstock;

import fr.uge.booqin.app.service.loan.BookInfo;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

class BookSupplyRecord {
        private static final Logger logger = Logger.getLogger(BookSupplyRecord.class.getName());

        private final BookInfo bookInfo;
        private int count;
        private final Set<UUID> tokens;

        public BookSupplyRecord(int count, BookInfo bookInfo) {
            this.bookInfo = bookInfo;
            this.count = count;
            this.tokens = new HashSet<>();
        }

        public UUID bookId() {
            return bookInfo.bookId();
        }

        public int count() {
            return count;
        }

        public void count(int count) {
            this.count = count;
        }

        public void addSupply(int count) {
            if(count < 1) {
                throw new IllegalArgumentException("Count must be positive");
            }
            this.count += count;
        }

        /// May return tokens if existing supply is less than count
        public List<UUID> removeSupply(int count) {
            if(count < 1) {
                throw new IllegalArgumentException("Count must be positive");
            }
            this.count = Math.min(0, this.count - count);
            // remove some tokens randomly (if any)
            var removedTokens = new HashSet<UUID>();
            var it = tokens.iterator();
            for (; count > 0 && it.hasNext(); count--) {
                var token = it.next();
                removedTokens.add(token);
                it.remove();
            }
            return List.copyOf(removedTokens);
        }

        public void removeSupply(Set<UUID> tokens) {
            var sizeBefore = this.tokens.size();
            this.tokens.removeAll(tokens);
            var sizeAfter = this.tokens.size();
            this.count += sizeBefore - sizeAfter;
        }

        public BookLock acquireLockAndGetToken(Duration duration) {
            if(count > 0) {
                var token = UUID.randomUUID();
                tokens.add(token);
                count--;
                return new BookLock(bookInfo, Instant.now().plus(duration), token);
            }
            logger.info("Acquiring a token for " + bookInfo.bookId());
            return null;
        }

        public void releaseLock(BookLock lock) {
            if(tokens.remove(lock.token())) {
                count++;
            }
            logger.info("Releasing a token for " + bookInfo.bookId());
        }

        public boolean hasToken(UUID token) {
            return tokens.contains(token);
        }



}