package fr.uge.booqin.app.service.notification;

import fr.uge.booqin.app.dto.notification.NotificationDTO;
import fr.uge.booqin.app.dto.notification.UserNotificationsDTO;
import fr.uge.booqin.app.dto.pagination.PageRequest;
import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.app.mapper.UserMapper;
import fr.uge.booqin.app.service.observer.obs_interface.TransactionObserver;
import fr.uge.booqin.app.service.observer.obs_interface.LoanObserver;
import fr.uge.booqin.domain.model.cart.TransactionStepType;
import fr.uge.booqin.domain.model.exception.OurFaultException;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.app.service.observer.obs_interface.FollowObserver;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.domain.model.notification.FollowNotification;
import fr.uge.booqin.domain.model.notification.Notification;
import fr.uge.booqin.domain.model.notification.InfoNotification;
import fr.uge.booqin.domain.model.notification.NotificationBase;
import fr.uge.booqin.domain.model.notification.TxStepUpdateNotification;
import fr.uge.booqin.infra.persistence.entity.notification.FollowNotificationEntity;
import fr.uge.booqin.infra.persistence.entity.notification.InfoNotificationEntity;
import fr.uge.booqin.infra.persistence.entity.notification.NotificationEntity;
import fr.uge.booqin.infra.persistence.entity.notification.TxStepNotificationEntity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.repository.notification.NotificationRepository;
import fr.uge.booqin.infra.persistence.repository.notification.UserNotificationsRepository;
import fr.uge.booqin.infra.persistence.repository.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.by;

@Service
@Transactional
public class NotificationService implements FollowObserver, LoanObserver, TransactionObserver {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationSseEmitterService notificationEmitter;
    private final UserNotificationsRepository userNotificationsRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               NotificationSseEmitterService notificationEmitter,
                               UserNotificationsRepository userNotificationsRepository
                               ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationEmitter = notificationEmitter;
        this.userNotificationsRepository = userNotificationsRepository;
    }

    @Transactional
    public UserNotificationsDTO getUserNotifications(User user, PageRequest pageRequest) {
        var pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.offset(),
                pageRequest.limit()
        );
        pageable.withSort(by("createdAt").ascending());
        Page<NotificationEntity> notificationsPage = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(user.id(), pageable);

        var userNotificationsEntity = userNotificationsRepository.findByUserId(user.id()).orElseThrow(
                () -> new OurFaultException("User notifications not found")
        );

        return new UserNotificationsDTO(
                userNotificationsEntity.getNumberOfUnread(),
                new PaginatedResult<>(
                        notificationsPage
                                .map(NotificationService::toModel)
                                .map(NotificationDTO::from)
                                .stream().collect(Collectors.toCollection(ArrayList::new)),
                        notificationsPage.getTotalElements(),
                        notificationsPage.getTotalPages(),
                        pageRequest.offset(),
                        pageRequest.limit()
                )
        );
    }

    @Override
    public void notifyFollow(User followerUser, User recipientUser) {
        var follower = userRepository.findById(followerUser.id())
                .orElseThrow(() -> new TheirFaultException("Follower not found"));
        var notification = new FollowNotificationEntity();
        notification.setFollower(follower);
        createSendNotification(recipientUser, notification);
    }

    @Override
    public void userAddedInWaitList(User user, String bookTitle) {
        createInfoNotification(user, "You are now on the wait list of " + bookTitle);
    }

    @Override
    public void propositionEvent(User user, String bookTitle) {
        createInfoNotification(user, "\"" + bookTitle + "\" is now available for you. You have 1h to accept it.");
    }

    @Override
    public void propositionExpiredCausedBySupplyDrop(User from, String title) {
        createInfoNotification(from, "The book \"" + title + "\" is no longer available." +
                " We placed you back on top of the wait list.");
    }

    @Override
    public void transactionUpdated(User origin, User target, Optional<UUID> orderId, UUID transactionId, TransactionStepType step) {
        var originEntity = userRepository.findByIdWithNotifications(origin.id())
                .orElseThrow(() -> new TheirFaultException("Origin not found"));
        var targetEntity = userRepository.findByIdWithNotifications(target.id())
                .orElseThrow(() -> new TheirFaultException("Target not found"));

        var notificationEntity = new TxStepNotificationEntity();
        notificationEntity.setTxId(transactionId);
        notificationEntity.setTxStepType(step.name());
        notificationEntity.setOtherUser(originEntity);
        orderId.ifPresent(notificationEntity::setOrderId);
        createSendNotification(UserMapper.from(targetEntity), notificationEntity);
    }

    @Transactional
    public void createInfoNotification(User user, String message) {
        var notificationEntity = new InfoNotificationEntity();
        notificationEntity.setMessage(message);
        createSendNotification(user, notificationEntity);
    }

    private void createSendNotification(User recipient, NotificationEntity notificationEntity) {
        var recipientEntity = userRepository.findByIdWithNotifications(recipient.id())
                .orElseThrow(() -> new TheirFaultException("Recipient not found"));
        createSendNotificationEntity(recipientEntity, notificationEntity);
    }

    private void createSendNotificationEntity(UserEntity recipientEntity, NotificationEntity notificationEntity) {
        var userNotificationsEntity = recipientEntity.getNotifications();
        notificationEntity.setRecipient(recipientEntity);
        notificationEntity.setCreatedAt(Instant.now());
        userNotificationsEntity.addNotification(notificationEntity);
        userNotificationsRepository.save(userNotificationsEntity);
        notificationEmitter.sendNotificationToUser(recipientEntity.getId(), NotificationDTO.from(toModel(notificationEntity)));
    }

    private static Notification toModel(NotificationEntity notificationEntity) {
        var base = new NotificationBase(
                notificationEntity.getId(),
                notificationEntity.getCreatedAt(),
                notificationEntity.isRead()
        );

        return switch (notificationEntity) {
            case FollowNotificationEntity fn -> new FollowNotification(
                    base,
                    fn.getFollower().getId(),
                    fn.getFollower().getUsername(),
                    fn.getFollower().getImageUrl()
            );
            case InfoNotificationEntity in -> new InfoNotification(base, in.getMessage());
            case TxStepNotificationEntity tx -> new TxStepUpdateNotification(
                    base,
                    tx.getOtherUser().getId(),
                    tx.getOtherUser().getUsername(),
                    tx.getOtherUser().getImageUrl(),
                    tx.getOrderId(),
                    tx.getTxId(),
                    tx.getTxStepType()
            );
            default ->
                    throw new OurFaultException("Unknown notification type: " + notificationEntity.getClass().getSimpleName());
        };
    }

    @Transactional
    public void deleteAllNotifications(List<UUID> notificationId, User user) {
        // notificationRepository.deleteAllByIdWithRecipient(notificationId, user.id());
        var recipientEntity = userRepository.findByIdWithNotifications(user.id())
                .orElseThrow(() -> new TheirFaultException("Recipient not found"));
        var userNotificationsEntity = recipientEntity.getNotifications();
        userNotificationsEntity.removeNotifications(notificationId);
    }

    @Async
    @Transactional
    public void notifyAll(User currentUser, String message) {
        if (!currentUser.isAdmin()) {
            throw new TheirFaultException("Only admins can send notifications to all users");
        }
        var users = userRepository.findAllWithNotifications();
        for (var user : users) {
            createInfoNotification(UserMapper.from(user), message);
        }
    }

}
