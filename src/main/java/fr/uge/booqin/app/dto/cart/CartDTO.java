package fr.uge.booqin.app.dto.cart;

import java.util.List;
import java.util.UUID;

public record CartDTO(UUID userId,
                      List<LockedBookDTO> books,
                      Long version,
                      Double estimatedPrice
    ) {
    }