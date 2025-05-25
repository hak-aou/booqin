package fr.uge.booqin.app.service.observer.implementation;

import fr.uge.booqin.app.service.book.BookService;
import fr.uge.booqin.app.service.collection.CollectionService;
import fr.uge.booqin.app.service.observer.obs_interface.VoteObserver;
import fr.uge.booqin.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VoteServiceObserver implements VoteObserver {
    private final CollectionService collectionService;
    private final BookService bookService;

    public VoteServiceObserver(CollectionService collectionService, BookService bookService) {
        this.collectionService = collectionService;
        this.bookService = bookService;
    }

    @Override
    public void notifyVote(User voterUser, UUID votableId) {
        var myVotedCollectionId = collectionService.findOrCreateVotedBooksCollection(voterUser);
        var votedBook = bookService.findBookByVotableId(votableId);

        votedBook.ifPresent(book -> collectionService.addBookToCollection(myVotedCollectionId, book.getId(), voterUser));
    }

    @Override
    public void notifyUnvote(User voterUser, UUID votableId) {
        var myVotedCollectionId = collectionService.findOrCreateVotedBooksCollection(voterUser);
        var votedBook = bookService.findBookByVotableId(votableId);

        votedBook.ifPresent(book -> collectionService.removeBookFromCollection(myVotedCollectionId, book.getId(), voterUser));
    }

}
