package fr.uge.booqin.app.dto.notification;

import fr.uge.booqin.app.dto.pagination.PaginatedResult;

public record UserNotificationsDTO (
            int unreadCount,
            PaginatedResult<NotificationDTO> notifications
    ) {}