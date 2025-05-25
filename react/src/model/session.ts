import {UserPrivateInfo} from "./userPublicInfo.ts";
import {UserCollectionInfo} from "./userCollectionInfo.ts";

interface LoginForm {
    usernameOrEmail: string;
    password: string;
    trustedDevice: boolean;
}

interface LoginResponse {
    accessToken: string;
}

interface AuthIdentity {
    id: string;
    email: string;
    isAdmin: boolean
}

interface LoggedSession {
    authIdentity?: AuthIdentity;
    user?: UserPrivateInfo;
    collections?: UserCollectionInfo[];
    accessToken: string;
    hasUnreadNotifications: boolean;
}

interface Session {
    isLogged: boolean;
    loggedSession?: LoggedSession;
}

export type {
    Session,
    AuthIdentity,
    LoggedSession,
    LoginResponse,
    LoginForm
};