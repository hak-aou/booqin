package fr.uge.booqin.app.service.observer.obs_interface;

import fr.uge.booqin.domain.model.User;

public interface FollowObserver {
    void notifyFollow(User followerUser, User recipientUser);
}