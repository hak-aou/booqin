import axios from "axios";
import {SessionMethods} from "../hooks/session/sessionContext.tsx";
import {API_ENDPOINTS} from "./endpoints.ts";

const privateAxios = axios.create({
    baseURL: API_ENDPOINTS.root,
    withCredentials: true,
    withXSRFToken: true,
    headers: {
        "Content-Type": "application/json",
    },
});

/**
 * This workflow is adapted
 * from the following article, by Max Shahdoost :
 * seen at https://maxtsh.medium.com/production-ready-comprehensive-anti-csrf-xss-reactjs-client-side-authentication-using-access-and-b2ade8ed8e19
 * on 2025-01-01
 */
export const booqinRequest = (sessionMethods: SessionMethods, requireAuth: boolean = false) => {
    const session = sessionMethods.session;
    const loggedSession = session.loggedSession;

    const request = axios.create({
        baseURL: API_ENDPOINTS.root,
        withCredentials: true,
        withXSRFToken: true,
        headers: {
            "Content-Type": "application/json",
        },
    });

    if (requireAuth && (!session.isLogged || loggedSession?.accessToken === "" || loggedSession?.accessToken === undefined)) {
        throw new Error("Request requires authentication, but no token is available");
    }

    if (session.isLogged) {
        const sessionDispatch = sessionMethods.dispatch;
        const token: string = loggedSession?.accessToken || "";
        request.defaults.headers.common["Authorization"] = `Bearer ${token}`;
        request.interceptors.response.use(
            function success(response) {
                return response;
            },
            async function failure(error) {
                if(!error.response) {
                    return Promise.reject(error);
                }
                const originalRequest = error?.config;
                if (!originalRequest || originalRequest._retry) {
                    return Promise.reject(error);
                }
                // Handle 401 Unauthorized (token expired)
                if (error?.response?.status === 401 && originalRequest.url !== API_ENDPOINTS.refresh) {
                    console.log("401 detected, attempting token refresh...");
                    originalRequest._retry = true;
                    try {
                        const res = await privateAxios.post(
                            API_ENDPOINTS.refresh,
                            {},
                            { withCredentials: true, withXSRFToken: true }
                        );
                        if (res?.status === 200) {
                            const accessToken = res.data.accessToken;
                            // 1. Update session with new token
                            sessionDispatch({ type: "refreshAccessToken", accessToken: accessToken });
                            // 2. Update the headers for the retried request
                            originalRequest.headers["Authorization"] = `Bearer ${accessToken}`;
                            // 3. Retry the original request with the new token
                            return axios(originalRequest);
                        }
                    } catch (err) {
                        console.error("Failed to refresh the token");
                        return Promise.reject(error);
                    }
                }
                return Promise.reject(error);
            }
        );
        return request;
    }

    return request;
};
