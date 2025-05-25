package fr.uge.booqin.app.controller.rest;

import fr.uge.booqin.app.dto.admin.PublishNotificationRequestDTO;
import fr.uge.booqin.app.service.notification.NotificationService;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final NotificationService notificationService;

    public AdminController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/notification")
    public void sendNotification(@RequestBody PublishNotificationRequestDTO dto, @AuthenticationPrincipal SecurityUser currentUser) {
        notificationService.notifyAll(currentUser.authenticatedUser(), dto.message());
    }
}
