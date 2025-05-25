import {needsAuth, ROUTES} from "../../routes/routes.ts";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {useEffect} from "react";
import {Navigate} from "react-router";
import {logout} from "../../api/api.ts";

interface LogoutProps {
    previous: string;
}

function Logout({previous = ROUTES.home.url}: LogoutProps) {
    const session = useSessionMethods()

    useEffect(() => {
        console.log("Logging out")
        logout(session);
    }, []);

    if (!session.isLogged) {
        return <Navigate to={needsAuth(previous) ? ROUTES.home.url : previous}/>;
    }

    return (
        <>
            <div className="flex justify-center min-h-screen ">
                <div className="w-3/4 p-4 rounded-lg  h-full mt-25">
                    Logout
                </div>
            </div>
        </>
    )
}

export default Logout