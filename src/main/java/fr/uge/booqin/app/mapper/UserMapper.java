package fr.uge.booqin.app.mapper;

import fr.uge.booqin.domain.model.Admin;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;

public interface UserMapper {
    static User from(UserEntity userEntity) {
        var user = User.of(userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                userEntity.getFollowable().getId()
        );
        return userEntity.getIsAdmin() ? Admin.of(user) : user;
    }
}
