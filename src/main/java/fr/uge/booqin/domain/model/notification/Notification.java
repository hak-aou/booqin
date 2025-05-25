package fr.uge.booqin.domain.model.notification;


public sealed interface Notification permits FollowNotification, InfoNotification, TxStepUpdateNotification {

        NotificationBase base();

}