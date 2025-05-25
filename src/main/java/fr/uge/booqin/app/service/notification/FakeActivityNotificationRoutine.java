package fr.uge.booqin.app.service.notification;

import com.github.javafaker.Faker;
import fr.uge.booqin.app.dto.notification.NotificationDTO;
import fr.uge.booqin.domain.model.notification.FollowNotification;
import fr.uge.booqin.domain.model.notification.NotificationBase;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

///
/// This class is responsible for sending fake notifications to the frontend.
///
@Service
public class FakeActivityNotificationRoutine {

    private final NotificationSseEmitterService sseService;
    private final Faker faker = new Faker();

    public FakeActivityNotificationRoutine(NotificationSseEmitterService sseService) {
        this.sseService = sseService;
    }
    
    //@Scheduled(fixedRate = 4000) // 8 seconds
    public void sendUserFollowNotifications() {
        var notification = new FollowNotification(
                new NotificationBase(UUID.randomUUID(), Instant.now(), false),
                UUID.randomUUID(), faker.name().username(), "");
        sseService.sendNotificationToAll(NotificationDTO.from(notification));
    }
}