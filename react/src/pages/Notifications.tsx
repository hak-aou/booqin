import React, {useEffect, useState} from "react";
import {useSessionMethods} from "../hooks/session/sessionContext.tsx";
import {DataPageScroller, PageRequest} from "../model/common.ts";
import {
    FollowNotification,
    InfoNotification,
    Notification,
    TxStepUpdate,
    UserNotifications
} from "../model/notification.ts";
import Loader from "../component/Loader.tsx";
import {formatDateWithTime} from "../utils/date.ts";
// import {FaCheck} from "react-icons/fa";
import {RxCross1} from "react-icons/rx";
import {useNavigate} from "react-router-dom";
import {ROUTES} from "../routes/routes.ts";

export default function Notifications() {
    const sessionMethods = useSessionMethods();
    const [loading, setLoading] = useState(true);
    const [deletedNotifications, setDeletedNotifications] = useState<string[]>([]);
    const navigate = useNavigate();

    const notificationFetcher = (page: PageRequest) =>
        sessionMethods.api.getNotifications(page).then((notifications: UserNotifications) => {
            console.log(notifications);
            return notifications.notifications;
        });

    const [scroller, setScroller] = useState<DataPageScroller<Notification>>(
        new DataPageScroller<Notification>([], 0, 0, 25, notificationFetcher)
    );

    useEffect(() => {
        getMoreNotifications().then();
    }, []);

    useEffect(() => {
        getMoreNotifications().then();
    },[sessionMethods.session.loggedSession?.hasUnreadNotifications])

    async function getMoreNotifications() {
        setLoading(true);
        try {
            setScroller(await scroller.fetchMoreData());
        } finally {
            setLoading(false);
        }
    }

    function handleRead(notificationId : string) {
        // @Todo Implement the logic to mark the notification as read
        console.log(notificationId);
    }

    function handleDelete(notificationId : string){
        sessionMethods.api.deleteNotifications([notificationId]).then(() => {
            setDeletedNotifications([...deletedNotifications, notificationId]);
        });
    }

    return <>
        <div className="max-w-4xl mx-auto px-4 py-8">
            <h1 className="text-2xl font-bold mb-6">Notifications</h1>
            <div className="space-y-4">
                {scroller.data
                .filter((notification) => !deletedNotifications.includes(notification.id))
                .map((notification) => (
                    <NotificationTemplate
                        key={notification.id}
                        notification={notification}
                        handleRead={() => handleRead(notification.id)}
                        handleDelete={() => handleDelete(notification.id)}
                    >
                        {notificationReader(notification, navigate)}
                    </NotificationTemplate>
                ))}
                {loading && <Loader/>}
            </div>
            {scroller.canFetchMore() && (
                <div className="mt-6 text-center">
                    <button
                        onClick={getMoreNotifications}
                        className="bg-teal-500 hover:bg-teal-600 text-white px-4 py-2 rounded-md transition-colors"
                    >
                        Load more
                    </button>
                </div>
            )}
        </div>
    </>
}

function notificationReader(notification: Notification, navigate: any) {

    switch (notification.type) {
        case "INFO":
            console.log(notification);
            const info = notification as InfoNotification;
            return (
                <div className="flex items-center gap-3">
                    <span className="text-gray-700">{info.message}</span>
                </div>
            );
        case "FOLLOW":
            const follow = notification as FollowNotification;
            return <div className="flex items-center gap-3">
                <div className="p-2 rounded-full bg-gray-100">
                    <img src={follow.avatar} alt={follow.username} className="w-8 h-8 rounded-full"/>
                </div>
                <span className="text-gray-700">
                    <span className="font-semibold">{follow.username}</span> started following you
                </span>
            </div>;
        case "TX_STEP":
            const txStepUpdate = notification as TxStepUpdate;
            return <div className="flex items-center gap-3">
                <div className="p-2 rounded-full bg-white border border-black">
                    <img src={txStepUpdate.avatar} alt={txStepUpdate.username}
                         className="w-8 h-8 rounded-full"/>
                </div>
                <span className="text-gray-700">
                        <span className="font-semibold hover:cursor-pointer hover:underline hover:text-blue-500"
                           onClick={() => {
                               if(txStepUpdate.orderId !== null){
                                   navigate(ROUTES.transaction.url.replace(':orderId', txStepUpdate.orderId).replace(":txId", txStepUpdate.txId));
                               } else {
                                   navigate(ROUTES.lendTransaction.url.replace(":txId", txStepUpdate.txId))
                               }
                           }}
                        >
                            {txStepUpdate.username} updated a transaction to {txStepUpdate.stepType}
                        </span>
                    </span>
            </div>;
        default:
            return <></>;

    }
}

// notification template allow component children to be passed

function NotificationTemplate({ children, notification, /*handleRead,*/ handleDelete }: {
    children: React.ReactNode,
    notification?: Notification,
    handleRead?: () => void,
    handleDelete?: () => void
}) {

    return (
        <div className="bg-white shadow-md rounded px-6 py-4 mb-3  ">
            <div className="flex flex-col gap-1">
                <div className="flex items-center justify-between">
                    {children}
                    <span className="text-xs text-gray-400">
                        {formatDateWithTime(notification?.createdAt)}
                    </span>
                </div>
                <div className="flex items-center justify-end gap-2 mt-2 ">
                    {/*{!notification?.read && (
                        <button onClick={handleRead} className="text-teal-500 hover:text-teal-700 p-4 hover:bg-gray-200 rounded-full hover:cursor-pointer">
                            <FaCheck />
                        </button>
                    )}*/}
                    <button onClick={handleDelete} className="text-red-500 hover:text-red-700 p-4 hover:bg-gray-200 rounded-full hover:cursor-pointer">
                        <RxCross1 />
                    </button>
                </div>
            </div>
        </div>
    );
}