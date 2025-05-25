import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {useNavigate} from "react-router-dom";
import {ROUTES} from "../../routes/routes.ts";
import {useState} from "react";

export default function Administration() {
    const sessionMethods = useSessionMethods();
    const navigate = useNavigate();
    const [notificationMessage, setNotificationMessage] = useState<string>("");

    if(!sessionMethods.user?.isAdmin) {
        navigate(ROUTES.home.url)
    }

    function submitNotification(e: React.FormEvent) {
        e.preventDefault();
        sessionMethods.api.sendNotificationToAll(notificationMessage);
    }

    return <>
        <div className="flex justify-center min-h-screen bg-gray-100">
            <div className="w-3/4 p-4 bg-white rounded-lg shadow-lg h-full">
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                        <div className="flex items-center gap-4">
                            <h2>Administration</h2>
                        </div>
                    </div>
                </div>
                <div className="w-2/3 mx-auto p-4 bg-white rounded-lg shadow mt-5 p-4">
                    <h3>Notification publisher</h3>
                    <form onSubmit={submitNotification} className="flex flex-col gap-2">
                        <input
                            type="text"
                            placeholder="Notification message"
                            onChange={(e) => setNotificationMessage(e.target.value)}
                            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        />
                        <button
                            type="submit"
                            className="
                            hover:cursor-pointer
                            bg-teal-500 hover:bg-teal-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                        >
                            Send to every connected users
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </>
}