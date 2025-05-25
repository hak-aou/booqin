package fr.uge.booqin.infra.persistence.entity.loan;

import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

@Entity
public class BookSupplyAndDemandEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private BookEntity book;

    @OneToMany
    private Set<LoanOfferEntity> lendings = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "bookSupplyAndDemandEntity")
    @OrderBy("timestamp ASC")
    private List<WaitingListEntryEntity> waitingQueue = new ArrayList<>();
    private int demand = 0;

    @Version
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<LoanOfferEntity> getLendings() {
        return lendings;
    }

    public void setLendings(Set<LoanOfferEntity> lendings) {
        this.lendings = lendings;
    }

    public BookEntity getBook() {
        return book;
    }

    public void setBook(BookEntity book) {
        this.book = book;
    }

    public int getDemand() {
        return demand;
    }

    public List<WaitingListEntryEntity> getWaitingQueue() {
        return waitingQueue;
    }

    public void setWaitingQueue(List<WaitingListEntryEntity> waitingQueue) {
        this.waitingQueue = new ArrayList<>(waitingQueue);
    }

    public void appendWaitingUser(UserEntity userEntity) {
        waitingQueue.add(waitingEntry(userEntity));
        demand++;
    }

    public void addOnTopOfWaitingQueue(UserEntity userEntity) {
        waitingQueue.addFirst(waitingEntry(userEntity));
        demand++;
    }

    private WaitingListEntryEntity waitingEntry(UserEntity userEntity) {
        var entry = new WaitingListEntryEntity();
        entry.setUser(userEntity);
        entry.setTimestamp(Instant.now());
        entry.setBookLoanOffersEntity(this);
        return entry;
    }

    public void removeWaitingUser(UserEntity userEntity) {
        waitingQueue.removeIf(entry -> entry.getUser().equals(userEntity));
    }

    public WaitingListEntryEntity pollWaitingUser() {
        var it = waitingQueue.iterator();
        if (!it.hasNext()) {
            return null;
        }
        var entry = it.next();
        it.remove();
        demand--;
        return entry;
    }

    public void decrementDemand() {
        demand--;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}