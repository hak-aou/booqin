@param org.springframework.security.web.csrf.CsrfToken _csrf
@param fr.uge.booqin.app.controller.mvc.session.SessionData session = null
@param String nonce


@param fr.uge.booqin.app.dto.collection.CollectionInfoDTO collection = null
@param fr.uge.booqin.app.dto.pagination.PaginatedResult<fr.uge.booqin.app.dto.book.BookInfoDTO> page = null

@template.layout.base(
_csrf = _csrf,
nonce = nonce,
session = session,
content = @`
    <div class="flex items-center justify-between">

        <div class="flex items-center gap-4">
            <div class="flex items-center gap-4">
                <h2 class="font-semibold">
                    ${collection.title()}
                    <%--Book count--%>

                </h2>
                <p class="inline-block px-2 py-1 text-sm rounded-md bg-green-100 text-green-800">
                    <%--<GiBlackBook class="inline-block"/--%> &nbsp;${collection.bookCount()}
                </p>
                <%--visibility--%>


                @if(session.isLogged() && session.user().id().equals(collection.owner().id()))
                    <p class="inline-block px-2 py-1 text-sm rounded-md ${collection.visibility() ? "bg-green-200 text-green-800" : "bg-red-200 text-red-800"}">
                            ${collection.visibility() ? "Public" : "Private"}
                    </p>
                @endif

                <%--SmartCollectin filters--%>
            </div>
        </div>
        <%--Owner--%>
        <div class="flex items-center gap-3 hover:cursor-pointer">
            <span class="flex items-center">${collection.owner().username()}</span>
            <img src="${collection.owner().imageUrl()}" alt="avatar" class="rounded-full w-8 h-8"/>
        </div>
    </div>
    <div class="">
        <h1 class="text-orange-500 font-semibold border-b-2 border-orange-500 pb-2 mb-4"></h1>
    </div>

    <%-- Follow --%>
    <div class="flex items-center justify-between">
        <div></div>
        <div class="flex items-center gap-4">
            <%--<FollowComponent followableId={followableId!}/>--%>
        </div>
    </div>

    <%-- Collection description --%>
    <div class="">
        <div class="text-gray-600 border-b-6 border-gray-200 p-2 border-r-0 border-l-0">
            ${collection.description()}
        </div>
    </div>

    <%-- Books  --%>
    <div class="bg-gray-100 p-4 border-b-6 border-gray-200 overflow-y-auto h-[50vh]">
        <div class="flex justify-between">
            <h1 class="text-orange-500 font-semibold">Books (${page.totalResults()})</h1>
        </div>
        <div class="p-1">
            @for(fr.uge.booqin.app.dto.book.BookInfoDTO book : page.data())
                <p>${book.title()}</p>
            @endfor
        </div>
    </div>

`)


<%--{smarCollection && <p
class={`inline-block px-2 py-1 text-sm rounded-md bg-blue-200 text-blue-800`}>
<LuBrain class="inline-block"/>&nbsp;Smart collection
</p>
}
</div>--%>
