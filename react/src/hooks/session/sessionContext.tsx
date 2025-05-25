import {createContext, Dispatch, useContext} from "react";
import {LoggedSession, Session, AuthIdentity} from "../../model/session";
import {UserPrivateInfo} from "../../model/userPublicInfo.ts";
import {Api, apiWithGivenSession, emptyApi} from "../../api/api.ts";
import {UserCollectionInfo} from "../../model/userCollectionInfo.ts";



export interface SessionMethods {
    dispatch: Dispatch<SessionAction>;
    session: Session;
    user: UserPrivateInfo | undefined;
    loggedAndAdmin: boolean;
    api: Api;
    isLogged: boolean;
    refreshAccessToken: (accessToken: string) => void;
    logout: () => void;
    setAuthIdentity: (authIdentity: AuthIdentity) => void;
    setAccountInfo: (user: UserPrivateInfo) => void;
    login: (loggedSession: LoggedSession) => void;
    setNotificationUnread : () => void;
    setCollections(collections: UserCollectionInfo[]): void;

}

export function useSessionMethods(): SessionMethods {
    const session = useSession();
    const dispatch = useSessionDispatch();
    return useSessionMethodsImpl(session, dispatch);
}

export function useSessionMethodsImpl(session: Session, dispatch: Dispatch<SessionAction>): SessionMethods {
    const methods : SessionMethods = {
        dispatch: dispatch,
        session: session,
        user: session.loggedSession?.user,
        loggedAndAdmin: session.loggedSession?.user?.isAdmin || false,
        api: emptyApi(),
        isLogged: session.isLogged,
        login: (loggedSession: LoggedSession) => dispatch({type: "login", loggedSession: loggedSession}),
        logout: () => dispatch({type: "logout"}),
        refreshAccessToken: (accessToken: string) => dispatch({type: "refreshAccessToken", accessToken: accessToken}),
        setAuthIdentity: (authIdentity: AuthIdentity) => dispatch({
            type: "setAuthIdentity",
            authIdentity: authIdentity
        }),
        setNotificationUnread: () => dispatch({type: "setNotificationUnread"}),
        setAccountInfo: (user: UserPrivateInfo) => dispatch({type: "setAccountInfo", user: user}),
        setCollections: (collections: UserCollectionInfo[]) => dispatch({type: "setCollections", collections: collections})
    };
    // We need to do this because we need to have the api method bound to the session,
    // and we can't do this in the object literal above (to use creds of the session and the hole auth flow)
    methods.api = apiWithGivenSession(methods);
    return methods;
}

// Session state
export const sessionContext = createContext<Session>({isLogged: false});
export function useSession() {
    return useContext(sessionContext)
}

// Modify the session state
export type SessionAction =
    | {type: "login", loggedSession: LoggedSession}
    | {type: "logout"}
    | {type: "refreshAccessToken", accessToken: string}
    | {type: "setAuthIdentity", authIdentity: AuthIdentity}
    | {type: "setAccountInfo", user: UserPrivateInfo}
    | {type: "setCollections", collections: UserCollectionInfo[]}
    | {type: "setNotificationUnread"};

export const sessionDispatchContext = createContext<Dispatch<SessionAction>>(() => {});
export function useSessionDispatch() {
    return useContext(sessionDispatchContext)
}

export function sessionReducer(state: Session, action: SessionAction): Session {
    switch (action.type) {
        case "login": {
            return {...state, isLogged: true, loggedSession: action.loggedSession};
        }
        case "logout":
            return {isLogged: false}
        case "setAuthIdentity":
            return {
                ...state,
                loggedSession: {...state.loggedSession, authIdentity: action.authIdentity} as LoggedSession
            }
        case "refreshAccessToken":
            return {
                ...state,
                loggedSession: {...state.loggedSession, isLogged: true, accessToken: action.accessToken} as LoggedSession
            }
        case "setAccountInfo":
            return {
                ...state,
                loggedSession: {...state.loggedSession, user: action.user} as LoggedSession
            }
        case "setCollections":
            return {
                ...state,
                loggedSession: {...state.loggedSession, collections: action.collections} as LoggedSession
            }
        case "setNotificationUnread":
            return {
                ...state,
                loggedSession: {...state.loggedSession, hasUnreadNotifications: true} as LoggedSession
            }
        default:
            return state
    }
}



