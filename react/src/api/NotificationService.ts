
import {Notification} from "../model/notification.ts";
import {SessionMethods} from "../hooks/session/sessionContext.tsx";
import {API_ENDPOINTS} from "./endpoints.ts";

class NotificationService {
    private eventSource: EventSource | null = null;
    private readonly BASE_URL = API_ENDPOINTS.root;

    async connect(sessionMethods: SessionMethods, onNotification: (notification: Notification) => void): Promise<void> {
        try {
            // First, get SSE token using bearer token
            const response = await sessionMethods.api.notificationToken();

            if (!response) {
                console.log('Failed to get SSE token');
                return Promise.reject();
            }

            const token = response.token;

            // Disconnect existing connection if any
            this.disconnect();

            // Connect using the SSE token
            this.eventSource = new EventSource(
                `${this.BASE_URL}${API_ENDPOINTS.notificationsSubscribe.replace(':token', token)}`,
            );

            this.eventSource.addEventListener('connect', () => {
                console.log('SSE Connection established');
            });

            this.eventSource.addEventListener('notification', (event: MessageEvent) => {
                try {
                    //console.log('Received notification:', event.data);
                    const notification: Notification = JSON.parse(event.data);
                    onNotification(notification);
                } catch (error) {
                    console.error('Error parsing notification');
                }
            });

            this.eventSource.onerror = () => {
                console.info('SSE Error, closing connection');
                this.disconnect();
                // Implement reconnection logic here if needed
            };

        } catch (error) {
            console.info('Failed to establish SSE connection:');
        }
    }

    disconnect(): void {
        if (this.eventSource) {
            this.eventSource.close();
            this.eventSource = null;
        }
    }
}

export default new NotificationService();