package fr.uge.booqin.app.service.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.uge.booqin.app.dto.notification.NotificationDTO;
import fr.uge.booqin.domain.model.exception.OurFaultException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

///
/// ## NotificationService
///
/// We use a list for each user to allow multiple tabs/devices. (userId -> list of emitters)
///
@Service
public class NotificationSseEmitterService implements ApplicationListener<ContextClosedEvent> {

    private volatile boolean isShuttingDown = false;
    private final Map<UUID, Set<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public SseEmitter openSseConnection(UUID userId) {
        if (isShuttingDown) {
            throw new OurFaultException("Server is shutting down, cannot create new SSE connections");
        }

        var emitter = new SseEmitter(Long.MAX_VALUE);
        // Create/add the user's emitter
        var userEmitterSet = userEmitters.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
        userEmitterSet.add(emitter);

        // Set callback clean up
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        // Initial message
        /*try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data(NotificationDTO.from()));
        } catch (IOException e) {
            removeEmitter(userId, emitter);
        }*/

        return emitter;
    }

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        var userEmitterSet = userEmitters.get(userId);
        if (userEmitterSet != null) {
            userEmitterSet.remove(emitter);
            // remove the entry if the user has no emitters left
            if (userEmitterSet.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    public void sendNotificationToUser(UUID userId, NotificationDTO notification) {
        if (isShuttingDown) {
            return;
        }
        var userEmitterSet = userEmitters.get(userId);
        if (userEmitterSet != null) {
            var deadEmitters = new HashSet<SseEmitter>();

            userEmitterSet.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("notification")
                            .data(objectMapper.writeValueAsString(notification)));
                } catch (IOException e) {
                    deadEmitters.add(emitter);
                }
            });

            // Clean up dead emitters
            deadEmitters.forEach(emitter -> removeEmitter(userId, emitter));
        }
    }

    public void sendNotificationToAll(NotificationDTO notification) {
        if (isShuttingDown) {
            return;
        }
        userEmitters.keySet().forEach(userId -> sendNotificationToUser(userId, notification));
    }

    @Override
    public void onApplicationEvent( ContextClosedEvent event) {
        isShuttingDown = true;
        // close all emitters
        userEmitters.values().forEach(emitterSet ->
                emitterSet.forEach(ResponseBodyEmitter::complete)
        );
    }

}
