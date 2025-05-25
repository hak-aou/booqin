@param org.springframework.security.web.csrf.CsrfToken _csrf
@param fr.uge.booqin.app.controller.mvc.session.SessionData session = null
@param String nonce
@param fr.uge.booqin.app.dto.pagination.PaginatedResult<fr.uge.booqin.app.dto.collection.CollectionInfoDTO> collections = null

@template.layout.base(
_csrf = _csrf,
nonce = nonce,
session = session,
content = @`
    <h1 class="text-2xl font-bold text-center mb-4">Collections</h1>
    <div class="space-y-4">
        @for(fr.uge.booqin.app.dto.collection.CollectionInfoDTO collection : collections.data())
            <a href="/collections/${collection.id()}">
                <div class="cursor-pointer bg-white shadow-lg rounded-xl p-4 hover:bg-gray-100 transition duration-200">
                    <div class="flex justify-between items-center">
                        <div>
                            <h2 class="text-lg font-semibold">${collection.title()}</h2>
                            <p class="text-gray-600">${collection.description()}</p>
                        </div>
                    </div>
                </div>
            </a>
        @endfor
    </div>
`)
