import {PaginatedResult} from "./common.ts";
import {TransactionStepType} from "./Cart.ts";

type NotificationType = 'FOLLOW' | 'INFO' | 'TX_STEP'

export interface Notification {
    type: NotificationType
    id: string
    createdAt: string
    read: boolean
}

export interface UserNotifications {
    notifications: PaginatedResult<Notification>
    unreadCount: number
}

export interface FollowNotification extends Notification  {
    followerId: string
    username: string
    avatar: string
}

export interface InfoNotification extends Notification  {
    message: string
}

export interface TxStepUpdate extends Notification {
    otherUserId: string
    username: string
    avatar: string
    txId: string
    orderId: string | null
    stepType: TransactionStepType
}

/// Token Used to authenticate when subscribing to notifications (SSE)
export interface NotificationToken {
    token: string
}