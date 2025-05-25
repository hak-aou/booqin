package fr.uge.booqin.app.service.observer.obs_interface;

import fr.uge.booqin.domain.model.User;

import java.util.UUID;

public interface VoteObserver {
    void notifyVote(User voterUser, UUID votableId);
    void notifyUnvote(User voterUser, UUID votableId);
}