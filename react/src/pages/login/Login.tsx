import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {Navigate} from "react-router";
import {booqinRequest} from "../../api/authentication.tsx";
import {LoggedSession, LoginForm, LoginResponse} from "../../model/session.ts";
import {FormEvent, useEffect, useState} from "react";
import {ROUTES} from "../../routes/routes.ts";
import {getAuthIdentity} from "../../api/api.ts";
import {API_ENDPOINTS} from "../../api/endpoints.ts";

interface LoginProps {
    previous: string;
}

function Login({previous = ROUTES.home.url}: LoginProps) {
    const sessionState = useSessionMethods()
    const [credentialsValid, setCredentialsValid] = useState<boolean>(false);
    const [loginForm, setLoginForm] = useState({usernameOrEmail: "", password: ""} as LoginForm);
    const [formSubmitted, setFormSubmitted] = useState<boolean>(false);
    const [formTouched, setFormTouched] = useState<boolean>(false);

    useEffect(() => {
        setFormTouched(true);
    }, [loginForm]);

    if (sessionState.isLogged) {
        return <Navigate to={previous}/>;
    }

    /**
     * Send a request to the API to get a token
     * @param form
     */
    function login(form: FormEvent) {
        form.preventDefault()
        booqinRequest(sessionState).post<LoginForm, any>(API_ENDPOINTS.login, loginForm)
            .then((response) => {
                const loginResponse: LoginResponse = response.data;
                const loggedSession: LoggedSession = {accessToken: loginResponse.accessToken, hasUnreadNotifications: false};
                sessionState.login(loggedSession);
                getAuthIdentity(sessionState);
                setCredentialsValid(true);
            }).catch((error: any) => {
                console.error(error);
                setCredentialsValid(false);
            }).then(() => {
                setFormTouched(false);
                setFormSubmitted(true);
        });
    }

    return (
        <>
            <div className="flex justify-center min-h-screen ">
                <div className="w-3/4 p-4 rounded-lg  h-full mt-25">
                    <form className="w-full max-w-sm mx-auto bg-white shadow-lg rounded px-8 pt-6 pb-8 mb-4"
                          onSubmit={login}>
                        <div className="md:flex items-center mb-6">
                            <div className="md:w-1/3">
                                <label className="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4"
                                       htmlFor="inline-full-name">
                                    Username
                                </label>
                            </div>
                            <div className="md:w-2/3">
                                <input
                                    className="bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full py-2 px-4 text-gray-700 leading-tight focus:outline-none focus:bg-white focus:border-teal-500"
                                    id="inline-full-name" type="text"
                                    onChange={(e) => setLoginForm({...loginForm, usernameOrEmail: e.target.value})}
                                />
                            </div>
                        </div>
                        <div className="md:flex md:items-center mb-6">
                            <div className="md:w-1/3">
                                <label className="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4"
                                       htmlFor="inline-password">
                                    Password
                                </label>
                            </div>
                            <div className="md:w-2/3">
                                <input
                                    className="bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full py-2 px-4 text-gray-700 leading-tight focus:outline-none focus:bg-white focus:border-teal-500"
                                    id="inline-password" type="password" placeholder="******************"
                                    onChange={(e) => setLoginForm({...loginForm, password: e.target.value})}
                                />
                            </div>
                        </div>
                        <div className="md:flex md:items-center mb-6">
                            <div className="md:w-1/3"></div>
                            <label className="md:w-2/3 block text-gray-500 font-bold">
                                <input
                                    className="mr-2 leading-tight"
                                    type="checkbox"
                                    onChange={(e) => setLoginForm({...loginForm, trustedDevice: e.target.checked})}
                                />
                                <span className="text-sm">
                                    Verify this device for 30 days
                                </span>
                            </label>
                        </div>
                        <div className="md:flex md:items-center">
                            <div className="md:w-1/3"></div>
                            <div className="md:w-2/3">
                                <button
                                    className="shadow bg-teal-600 hover:bg-teal-500 hover:cursor-pointer focus:shadow-outline focus:outline-none text-white font-bold py-2 px-4 rounded"
                                    type="submit">
                                    Sign in
                                </button>
                            </div>
                        </div>
                        { !formTouched && !credentialsValid && formSubmitted &&
                            <div className="md:flex md:items-center mt-4 text-red-500 font-semibold">
                                <div className="md:w-1/3"></div>
                                <div className="md:w-2/3">
                                    <p>Invalid credentials</p>
                                </div>
                            </div>
                        }
                    </form>
                </div>
            </div>
        </>
    )
}

export default Login