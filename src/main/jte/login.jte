@param String error = null
@param String logout = null
@param org.springframework.security.web.csrf.CsrfToken _csrf
@param fr.uge.booqin.app.controller.mvc.session.SessionData session = null
@param String nonce

@template.layout.base(
_csrf = _csrf,
    nonce = nonce,
session = session,
content = @`
    <div class="flex justify-center min-h-screen ">
        <div class="w-3/4 p-4 rounded-lg  h-full mt-25">
            <form
                    nonce="${nonce}"
                    action="/login" method="post" class="w-full max-w-sm mx-auto bg-white shadow-lg rounded px-8 pt-6 pb-8 mb-4">
                <input type="hidden" name="${_csrf.getParameterName()}" value="${_csrf.getToken()}">
                <div class="md:flex items-center mb-6">
                    <div class="md:w-1/3">
                        <label class="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4"
                               for="username">Username:
                        </label>
                    </div>
                    <div class="md:w-2/3">
                        <input
                                type="text" id="username"
                                class="bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full py-2 px-4 text-gray-700 leading-tight focus:outline-none focus:bg-white focus:border-teal-500"
                                name="username" required
                        />
                    </div>
                </div>
                <div class="md:flex md:items-center mb-6">
                    <div class="md:w-1/3">
                        <label class="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4"
                               for="password">Password:</label>
                    </div>
                    <div class="md:w-2/3">
                        <input
                                class="bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full py-2 px-4 text-gray-700 leading-tight focus:outline-none focus:bg-white focus:border-teal-500"
                                type="password" id="password" name="password" required placeholder="******************"
                        />
                    </div>
                </div>
                <div class="md:flex md:items-center">
                    <div class="md:w-1/3"></div>
                    <div class="md:w-2/3">
                        <button
                                class="shadow bg-teal-600 hover:bg-teal-500 hover:cursor-pointer focus:shadow-outline focus:outline-none text-white font-bold py-2 px-4 rounded"
                                type="submit">
                            Sign in
                        </button>
                    </div>
                </div>
            </form>
            @if(error != null)
                <div style="color: red;">
                    Invalid username or password
                </div>
            @endif
        </div>
    </div>

`)

<%--
@template.layout.base(
_csrf = _csrf,
currentUser = currentUser,
session = session,
    content = @`
        <form action="/login" method="post">
            <input type="hidden" name="${_csrf.getParameterName()}" value="${_csrf.getToken()}">
            <div>
                <label for="username">Username:</label>
                <input type="text" id="username" name="username" required value="admin"/>
            </div>
            <div>
                <label for="password">Password:</label>
                <input type="password" id="password" name="password" required value="adminpass"/>
            </div>
            <button type="submit">Login</button>
        </form>
    @if(error != null)
        <div style="color: red;">
            Invalid username or password
        </div>
    @endif
`)
--%>

