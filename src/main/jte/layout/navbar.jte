
@param fr.uge.booqin.app.controller.mvc.session.SessionData session = null

<header class="bg-white dark:bg-gray-900 border-b-1 border-gray-200">
    <div class="mx-auto max-w-screen-xl px-4 sm:px-6 lg:px-8">
        <div class="flex h-16 items-center justify-between">
            <div class="md:flex md:items-center md:gap-12">
                <a class="block text-teal-600 dark:text-teal-600 hover:cursor-pointer"
                    href="/"
                >
                <span class="sr-only">Home</span>
                <h1 class="text-2xl font-bold">BooqIn</h1>
                </a>
            </div>
            <div class="flex-1 flex items-center justify-center gap-4">
                <div class="hidden md:block">
                    <nav aria-label="Global">
                        <ul class="flex items-center gap-6 text-sm">
                            @for(var item : java.util.List.of("borrows", "transactions"))
                                <li class="">
                                    <a href="${item}"
                                            class="text-gray-500 transition hover:text-gray-500/75 dark:text-white dark:hover:text-white/75 hover:cursor-pointer" >
                                        ${item}
                                    </a>
                                </li>
                            @endfor
                        </ul>
                    </nav>
                </div>
                <div class="hidden md:block">
                    <%--<SearchBarHome/>--%>
                </div>
            </div>

            <div class="flex items-center gap-4">
                <div class="sm:flex sm:gap-4">
                    @if(session.isLogged())
                        <div class="hidden sm:flex hover:cursor-pointer">
                            <a
                                    href="/logout"
                                    class="rounded-md bg-gray-100 px-5 py-2.5 text-sm font-medium text-teal-600 dark:bg-gray-800
                                            hover:bg-gray-200
                                            dark:text-white dark:hover:text-white/75"
                            >
                                Logout
                            </a>
                        </div>
                        <div class="hidden sm:flex hover:cursor-pointer transition duration-75 ease-in-out hover:-translate-y-1 hover:scale-110">
                            <img src="${session.profile().publicProfileDto().imageUrl()}" class="rounded-full w-10 h-10" />
                        </div>
                        <div class="hidden sm:flex items-center justify-center hover:cursor-pointer"
                                <%--onClick={() => navigate(ROUTES.notifications.url)}--%>
                        >
                            <svg
                                    size="26"
                                    class="text-gray-500 dark:text-white dark:hover:text-white/75"
                                    stroke="currentColor" fill="currentColor" stroke-width="0" viewBox="0 0 512 512" height="26px" width="26px"
                                    xmlns="http://www.w3.org/2000/svg">
                                <path d="M256 464c22.779 0 41.411-18.719 41.411-41.6h-82.823c0 22.881 18.633 41.6 41.412 41.6zm134.589-124.8V224.8c0-63.44-44.516-117.518-103.53-131.041V79.2c0-17.682-13.457-31.2-31.059-31.2s-31.059 13.518-31.059 31.2v14.559c-59.015 13.523-103.53 67.601-103.53 131.041v114.4L80 380.8v20.8h352v-20.8l-41.411-41.6z">
                                </path>
                            </svg>
                        </div>

                        <div class="hidden sm:flex items-center justify-center hover:cursor-pointer">
                            <svg
                                    stroke="currentColor"
                                 fill="currentColor"
                                 stroke-width="0"
                                 viewBox="0 0 24 24"
                                 height="26px"
                                 width="26px"
                                    class="text-gray-500 dark:text-white dark:hover:text-white/75 hover:text-gray-500/75" size="26"

                                 xmlns="http://www.w3.org/2000/svg">
                                    <path fill="none" d="M0 0h24v24H0z"></path>
                                    <path d="m17.21 9-4.38-6.56a.993.993 0 0 0-.83-.42c-.32 0-.64.14-.83.43L6.79 9H2c-.55 0-1 .45-1 1 0 .09.01.18.04.27l2.54 9.27c.23.84 1 1.46 1.92 1.46h13c.92 0 1.69-.62 1.93-1.46l2.54-9.27L23 10c0-.55-.45-1-1-1h-4.79zM9 9l3-4.4L15 9H9zm3 8c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z">
                                </path>
                            </svg>
                        </div>
                    @else
                        <a
                                href="/login"
                                class="rounded-md bg-teal-600 px-5 py-2.5 text-sm font-medium
                                hover:bg-teal-500 hover:cursor-pointer
                                text-white shadow-sm dark:hover:bg-teal-500">
                            Login
                        </a>
                        <div class="hidden sm:flex hover:cursor-pointer">
                            <a class="rounded-md bg-gray-100 px-5 py-2.5 text-sm font-medium text-teal-600 dark:bg-gray-800
                                            hover:bg-gray-200
                                            dark:text-white dark:hover:text-white/75"
                            >
                                Sign Up
                            </a>
                        </div>
                    @endif
                </div>
                <%--<div class="block md:hidden">
                    <button
                            class="rounded-sm
                                                hover:cursor-pointer
                                                bg-gray-100 p-2 text-gray-600 transition hover:text-gray-600/75 dark:bg-gray-800 dark:text-white dark:hover:text-white/75"
                            --%><%--onClick={() => setWrapped(!wrapped)}--%><%--
                    >
                    <svg
                            xmlns="http://www.w3.org/2000/svg"
                            class="size-5"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                            stroke-width="2"
                    >
                        <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16"/>
                    </svg>
                    </button>
                </div>--%>
        </div>
        </div>
</div>
<%--{ // Mobile menu
wrapped && <div class="md:hidden">
    <nav aria-label="Global">
        <ul class="flex flex-col gap-4 items-center text-sm">
            {[ROUTES.waitlists, ROUTES.lend].map((route, index) => (
            <React.Fragment key={route.url + index}>
                {(!route.needAuth || session.isLogged) && (
                <li class="w-full border-b-1 border-gray-200 dark:border-gray-700 text-center">
                    <a class="text-gray-500 transition hover:text-gray-500/75 dark:text-white dark:hover:text-white/75 hover:cursor-pointer"
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
            <li class="w-full border-b-1 border-gray-200 dark:border-gray-700 text-center sm:hidden">
                <a class="text-gray-500 transition hover:text-gray-500/75 dark:text-white dark:hover:text-white/75 hover:cursor-pointer">
                    Sign Up
                </a>
            </li>
        </>}
        {!isCurrentRoute(ROUTES.logout.url) && session.isLogged && <>
        <li class="w-full border-b-1 border-gray-200 dark:border-gray-700 text-center sm:hidden">
            <a class="text-gray-500 transition hover:text-gray-500/75 dark:text-white dark:hover:text-white/75 hover:cursor-pointer"
               onClick={() => navigate(ROUTES.logout.url)}
            >
            Logout
            </a>
        </li>
    </>}
    </ul>
    </nav>
</div>
}--%>
</header>