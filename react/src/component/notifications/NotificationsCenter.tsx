import React, {useEffect, useCallback, useState} from 'react';

import NotificationService from "../../api/NotificationService.ts";
import {FollowNotification, InfoNotification, Notification} from "../../model/notification.ts";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {toastFollow} from "./toast/toastFollow.tsx";
import {toastInfo} from "./toast/toastInfo.tsx";
// import {toast} from "react-toastify";

export const NotificationsCenter: React.FC = () => {
    const session = useSessionMethods();
    const [sseConnected, setSseConnected] = useState(false);

    const handleNotification = useCallback((notification: Notification) => {
        session.setNotificationUnread();
        switch (notification.type) {
            case 'FOLLOW':
                const followNotification = notification as FollowNotification;
                toastFollow(followNotification);
                break;
            case 'INFO':
                toastInfo(notification as InfoNotification);
                break;
            case 'TX_STEP':
                console.log('TX_STEP_UPDATE', notification);
                break;
            default:
                console.error('Unknown notification type:', notification.type);
        }
    }, []);

    const connectToSSE = async () => {
        try {
            await NotificationService.connect(session, (notification) => {
                setSseConnected(true);
                handleNotification(notification);
            }).catch(() => {
                console.error('Failed to connect to notifications');
                setSseConnected(false);
            });
        } catch (error) {
            console.error('Failed to connect to notifications');
            setSseConnected(false);
        }
    };

    useEffect(() => {
        if(session.isLogged) {
            connectToSSE().then();
            return () => {
                NotificationService.disconnect();
                setSseConnected(false);
            };
        } else {
            console.log('Not logged in, skipping notifications');
        }
    }, [session.isLogged, handleNotification]);

    // retry sse connection after 20 seconds if disconnected
    useEffect(() => {
        setTimeout(() => {
            if (session.isLogged && !sseConnected) {
                console.log('Retrying to connect to notifications');
                connectToSSE().then();
            }
        }, 2000);
    }, [sseConnected]);

    return null;
};