package fr.uge.booqin.infra.external.avatar;

import org.springframework.stereotype.Component;

@Component
public class DiceBear implements AvatarGenerator {
    public String generateAvatar(String seed) {
        return "https://api.dicebear.com/9.x/thumbs/svg?seed=" + seed;
    }
}
