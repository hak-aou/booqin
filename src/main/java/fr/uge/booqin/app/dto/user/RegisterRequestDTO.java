package fr.uge.booqin.app.dto.user;

import java.util.List;

public record RegisterRequestDTO(
        String username,
        String email,
        List<String> phrase
) {
}
