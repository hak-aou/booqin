package fr.uge.booqin.infra.external.avatar;

public interface AvatarGenerator {

    /**
     * Generate an avatar from a seed.
     *
     * @param seed the seed to generate the avatar from
     * @return the URL of the image generated
     */
    String generateAvatar(String seed);

}
