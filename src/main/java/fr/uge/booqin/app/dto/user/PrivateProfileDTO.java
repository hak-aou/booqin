package fr.uge.booqin.app.dto.user;

import com.fasterxml.jackson.annotation.JsonUnwrapped;


public record PrivateProfileDTO(
        @JsonUnwrapped PublicProfileDTO publicProfileDto,
        String email,
        boolean isAdmin
        // add other private stuff here
) {

}
