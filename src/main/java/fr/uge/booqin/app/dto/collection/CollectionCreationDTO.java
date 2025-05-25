package fr.uge.booqin.app.dto.collection;

public record CollectionCreationDTO(
        String title,
        String description,
        Boolean visibility
) {
}
