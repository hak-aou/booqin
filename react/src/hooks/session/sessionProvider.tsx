import {
    sessionContext,
    sessionDispatchContext,
    sessionReducer, useSessionMethodsImpl
} from "./sessionContext";
import {ReactNode, useEffect, useReducer} from "react";
import {booqinRequest} from "../../api/authentication.tsx";
import {LoggedSession, LoginResponse} from "../../model/session.ts";
import {getAuthIdentity} from "../../api/api.ts";
import {API_ENDPOINTS} from "../../api/endpoints.ts";
import {UserNotifications} from "../../model/notification.ts";

export function SessionProvider({children} : {children: ReactNode}) {
    const [session, sessionDispatch] = useReducer(sessionReducer, {isLogged: false});
    const sessionMethods = useSessionMethodsImpl(session, sessionDispatch);

    // try to autologin by refreshing the token if possible
    useEffect(() => {
        if(!session.isLogged) {
            booqinRequest(sessionMethods).post(API_ENDPOINTS.refresh, {}, {withCredentials: true, withXSRFToken: true})
                .then((response) => {
                    const loginResponse: LoginResponse = response.data;
                    const loggedSession: LoggedSession = {accessToken: loginResponse.accessToken, hasUnreadNotifications: false};
                    sessionMethods.login(loggedSession);
                })
                .catch(() => console.log("No token refresh possible, autologin "));
        }
    }, []);

    /*
        This is where we do all the initial requests
        once the user is authenticated
     */
    useEffect(() => {
        if(session.isLogged) {
            getAuthIdentity(sessionMethods);
            sessionMethods.api.fetchAndUpdateMyUserAccountInfo();
            sessionMethods.api.fetchAndUpdateMyNotifications(0, 10)
                .then((notifications: UserNotifications) => {
                    console.debug(notifications);
                }).catch((error) => {
                    console.error(error);
                });
            sessionMethods.api.fetchAndUpdateMyCollections();
        }
    }, [session.isLogged]);

    return (
        <sessionContext.Provider value={session}>
            <sessionDispatchContext.Provider value={sessionDispatch}>
                {children}
            </sessionDispatchContext.Provider>
        </sessionContext.Provider>
    );
}