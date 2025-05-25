import {useSession} from "../hooks/session/sessionContext.tsx";
import {ReactNode} from "react";
import {Navigate, useLocation} from 'react-router';

/**
 * ProtectedRoute component
 * It's a wrapper to protect routes that require authentication
 * @param children anything
 * @constructor
 */
const ProtectedRoute = ({ children } : {children: ReactNode}) => {
    const session  = useSession();
    const location = useLocation();

    if (!session.isLogged) {
        console.log("User not logged in, redirecting to /login");
        const currentLocation = location.pathname + location.search;
        return <Navigate to="/login" state={{previous: currentLocation}} />;
    }

    return children;
};

export default ProtectedRoute;