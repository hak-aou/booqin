import {NavigateOptions, useNavigate} from "react-router-dom";
import {useState} from "react";
import {useSession} from "../../hooks/session/sessionContext.tsx";
import {ROUTES} from "../../routes/routes.ts";
import React from "react";
import {IoMdNotifications} from "react-icons/io";
import {MdShoppingBasket} from "react-icons/md";
import SearchBarHome from "../search/SearchBar.tsx";

export default function Navbar() {
    const navigate = useNavigate();
    const [wrapped, setWrapped] = useState(false);
    const session = useSession()
    const currentUrlWithoutHost = window.location.pathname + window.location.search;

    function isCurrentRoute(route: string) {
        return window.location.pathname.startsWith(route);
    }

    return <>
        <header className="bg-white dark:bg-gray-900 border-b-1 border-gray-200">
            <div className="mx-auto max-w-screen-xl px-4 sm:px-6 lg:px-8">
                <div className="flex h-16 items-center justify-between">
                    <div className="md:flex md:items-center md:gap-12">
                        <a className="block text-teal-600 dark:text-teal-600 hover:cursor-pointer"
                           onClick={() => navigate(ROUTES.home.url)}>
                            <span className="sr-only">Home</span>
                            <h1 className="text-2xl font-bold">BooqIn</h1>
                        </a>
                    </div>
                    <div className="flex-1 flex items-center justify-center gap-4">
                        <div className="hidden md:block">
                            <nav aria-label="Global">
                                <ul className="flex items-center gap-6 text-sm">
                                    {[ROUTES.borrow, ROUTES.lendTransactions].map((route, index) => (
                                        <React.Fragment key={route.url + index}>
                                            {(!route.needAuth || session.isLogged) && (
                                                <li className={`${isCurrentRoute(route.url) || (route.url.startsWith('/borrow') && isCurrentRoute("/borrow")) ? 'border-b-2 border-primary' : ''}`}>
                                                    <a className={`text-gray-500 transition hover:text-gray-500/75 dark:text-white 
                                                    dark:hover:text-white/75 hover:cursor-pointer ${isCurrentRoute(route.url) ? 'text-teal-600 dark:text-teal-600' : ''}`}
                                                       onClick={() => navigate(route.url)}
                                                    >
                                                        {route.label}
                                                    </a>
                                                </li>
                                            )}
                                        </React.Fragment>
                                    ))}
                                </ul>
                            </nav>
                        </div>
                        <div className="hidden md:block">
                            <SearchBarHome/>
                        </div>
                        <div className="hidden md:block">
                            <ul className="flex items-center gap-6 text-sm">
                                {session.isLogged && session.loggedSession?.user?.isAdmin && (
                                    <li className={`${isCurrentRoute(ROUTES.admin.url)} ? 'border-b-2 border-primary' : ''}`}>
                                        <a className={`text-gray-500 transition hover:text-gray-500/75 dark:text-white 
                                            dark:hover:text-white/75 hover:cursor-pointer ${isCurrentRoute(ROUTES.admin.url) ? 'text-teal-600 dark:text-teal-600' : ''}`}
                                           onClick={() => navigate(ROUTES.admin.url)}
                                        >
                                            {ROUTES.admin.label}
                                        </a>
                                    </li>
                                )}
                            </ul>
                        </div>
                    </div>

                    <div className="flex items-center gap-4">
                        <div className="sm:flex sm:gap-4">
                            {!(isCurrentRoute(ROUTES.login.url) || session.isLogged) &&
                                <a
                                    className="rounded-md bg-teal-600 px-5 py-2.5 text-sm font-medium
                                hover:bg-teal-500 hover:cursor-pointer
                                text-white shadow-sm dark:hover:bg-teal-500"
                                    onClick={() => navigate(ROUTES.login.url)}
                                >
                                    Login
                                </a>
                            }
                            {!isCurrentRoute(ROUTES.logout.url) && session.isLogged && <>
                                <div className="hidden sm:flex hover:cursor-pointer">
                                    <a
                                        className="rounded-md bg-gray-100 px-5 py-2.5 text-sm font-medium text-teal-600 dark:bg-gray-800
                                    hover:bg-gray-200
                                    dark:text-white dark:hover:text-white/75"
                                        onClick={() => navigate(ROUTES.logout.url, {
                                            replace: true,
                                            state: {currentUrlWithoutHost}
                                        } as NavigateOptions)}
                                    >
                                        Logout
                                    </a>
                                </div>
                                <div className="hidden sm:flex hover:cursor-pointer
                                transition duration-75 ease-in-out hover:-translate-y-1 hover:scale-110
                                "
                                     onClick={() => navigate(ROUTES.profile.url)}>
                                    <img src={session.loggedSession?.user?.imageUrl} alt="avatar" className="rounded-full w-10 h-10"/>

                                </div>
                                <div className="hidden sm:flex items-center justify-center hover:cursor-pointer"
                                    onClick={() => navigate(ROUTES.notifications.url)}
                                >
                                    <IoMdNotifications className={`text-gray-500 dark:text-white dark:hover:text-white/75 
                                    ${session.loggedSession?.hasUnreadNotifications ? 'text-orange-600 dark:text-orange-600' : ''}
                                    hover:text-gray-500/75`} size="26"/>
                                </div>
                                <div className="hidden sm:flex items-center justify-center hover:cursor-pointer"
                                     onClick={() => navigate(ROUTES.cart.url)}
                                >
                                    <MdShoppingBasket className={`text-gray-500 dark:text-white dark:hover:text-white/75 
                                    hover:text-gray-500/75`} size="26"/>
                                </div>
                            </>}
                            {!isCurrentRoute(ROUTES.signUp.url) && !session.isLogged && <>
                                <div className="hidden sm:flex hover:cursor-pointer">
                                    <a
                                        className="rounded-md bg-gray-100 px-5 py-2.5 text-sm font-medium text-teal-600 dark:bg-gray-800
                                    hover:bg-gray-200
                                    dark:text-white dark:hover:text-white/75"
                                        onClick={() => navigate(ROUTES.signUp.url)}
                                    >
                                        Sign Up
                                    </a>
                                </div>
                            </>}
                        </div>

                        <div className="block md:hidden">
                            <button
                                className="rounded-sm
                                hover:cursor-pointer
                                bg-gray-100 p-2 text-gray-600 transition hover:text-gray-600/75 dark:bg-gray-800 dark:text-white dark:hover:text-white/75"
                                onClick={() => setWrapped(!wrapped)}
                            >
                                <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    className="size-5"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                >
                                    <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16"/>
                                </svg>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            { // Mobile menu
                wrapped && <div className="md:hidden">
                    <nav aria-label="Global">
                        <ul className="flex flex-col gap-4 items-center text-sm">
                            {[ROUTES.waitlists, ROUTES.lend].map((route, index) => (
                                <React.Fragment key={route.url + index}>
                                    {(!route.needAuth || session.isLogged) && (
                                        <li className="w-full border-b-1 border-gray-200 dark:border-gray-700 text-center">
                                            <a className="text-gray-500 transition hover:text-gray-500/75 dark:text-white dark:hover:text-white/75 hover:cursor-pointer"
                                               onClick={() => navigate(route.url)}
                                            >
                                                {route.label}
                                            </a>
                                        </li>
                                    )}
                                </React.Fragment>
                            ))}
                            {/* ----- Special style links ----- */}
                            {!isCurrentRoute(ROUTES.signUp.url) && !session.isLogged && <>
                                <li className="w-full border-b-1 border-gray-200 dark:border-gray-700 text-center sm:hidden">
                                    <a className="text-gray-500 transition hover:text-gray-500/75 dark:text-white dark:hover:text-white/75 hover:cursor-pointer">
                                        Sign Up
                                    </a>
                                </li>
                            </>}
                            {!isCurrentRoute(ROUTES.logout.url) && session.isLogged && <>
                                <li className="w-full border-b-1 border-gray-200 dark:border-gray-700 text-center sm:hidden">
                                    <a className="text-gray-500 transition hover:text-gray-500/75 dark:text-white dark:hover:text-white/75 hover:cursor-pointer"
                                       onClick={() => navigate(ROUTES.logout.url)}
                                    >
                                        Logout
                                    </a>
                                </li>
                            </>}
                        </ul>
                    </nav>
                </div>
            }
        </header>
    </>
}