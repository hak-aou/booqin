package fr.uge.booqin.app.dto.cart;

import java.util.UUID;

public record OwnerProfile(UUID id,
                           String username,
                           String avatar)
    {}
