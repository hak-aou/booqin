package fr.uge.booqin.app.service.observer.obs_interface;

import fr.uge.booqin.domain.model.User;

public interface LoanObserver {
    void userAddedInWaitList(User user, String bookTitle);
    void propositionEvent(User user, String bookTitle);
    void propositionExpiredCausedBySupplyDrop(User from, String title);
}
