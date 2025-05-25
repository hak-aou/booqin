import {useNavigate, useParams} from "react-router-dom";
import {useEffect, useRef, useState} from "react";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {ROUTES} from "../../routes/routes.ts";
import {GiBlackBook} from "react-icons/gi";
import {DataPageScroller, PageRequest, PaginatedResult} from "../../model/common.ts";
import {BookInfo} from "../../model/book.ts";
import FollowComponent from "../../component/followers/FollowComponent.tsx";
import Loader from "../../component/Loader.tsx";
import BookPreviewComponent from "../../component/books/BookComponent.tsx";
import {BookFilter} from "../../component/search/filter/FilterComponent.tsx";
import {FilterBooksDTO} from "../../model/filter.ts";
import {UserCollectionInfo} from "../../model/userCollectionInfo.ts";
import {CommentSection} from "../../component/comment/CommentSection.tsx";
import {UserSmartCollectionInfo} from "../../model/usersSmartCollectionInfo.ts";
import {LuBrain} from "react-icons/lu";
import {MdModeEdit} from "react-icons/md";
import {FaTrash} from "react-icons/fa";
import {IoMdCheckmarkCircle} from "react-icons/io";
import {AiOutlineRollback} from "react-icons/ai";
import ConfirmationModal from "../../component/ConfirmationModal.tsx";

const CollectionDetail = () => {
    const {collectionId} = useParams<{ collectionId: string }>();
    const [collection, setCollection] = useState<UserCollectionInfo | undefined>(undefined);
    const [smartCollection, setSmartCollection] = useState<UserSmartCollectionInfo | undefined>(undefined);
    const [collectionUpdateForm, setCollectionUpdateForm] = useState<UserSmartCollectionInfo | undefined>(undefined);
    const [editMode, setEditMode] = useState(false);
    const [deleteModal, setDeleteModal] = useState(false);

    const sessionMethods = useSessionMethods();
    const [bookLoading, setBookLoading] = useState(false);
    const navigate = useNavigate();

    const bookFetcher = async (page: PageRequest, filter?: FilterBooksDTO) => {
        if (filter !== undefined) {
            const response = await sessionMethods.api.filterBooks(Number(collectionId), filter, page);
            console.log('with filter', response);
            return response;
        } else {
            const response = await sessionMethods.api.getBooksOfCollection(Number(collectionId), page);
            console.log('without filter', response);
            return response;
        }

    }

    const [scroller, setScroller] = useState<DataPageScroller<BookInfo>>(
        new DataPageScroller<BookInfo>([], 0, 0, 25, bookFetcher)
    );

    async function getMoreBooks() {
        setBookLoading(true);
        try {
            setScroller(await scroller.fetchMoreData());
        } finally {
            setBookLoading(false);
        }
    }

    async function onRefreshFilter(books: PaginatedResult<BookInfo>,
                                   newFilter: FilterBooksDTO) {
        setBookLoading(true);
        try {
            localStorage.setItem(`collectionFilter_${collectionId}`, JSON.stringify(newFilter));

            setScroller(new DataPageScroller(
                books.data,
                books.totalResults,
                books.data.length,
                25,
                (page) => bookFetcher(page, newFilter)
            ))

        } finally {
            setBookLoading(false);
        }
    }

    useEffect(() => {
        fetchCollection();
    }, [collectionId]);

    function fetchCollection() {
        const fetchData = async () => {

                const saved = localStorage.getItem(`collectionFilter_${collectionId}`);
                const localFilter = saved ? JSON.parse(saved) : undefined;

                if(localFilter) {
                    const books = await bookFetcher({ offset: 0, limit: 25 }, localFilter);
                    await onRefreshFilter(books, localFilter);
                } else {
                    getMoreBooks().then();
                }

                const collectionIdAsLong = Number(collectionId);
                console.log("collectionId:", collectionId);
                sessionMethods.api.getCollection(collectionIdAsLong).then((collectionData) => {
                    console.log("collection:", collectionData);
                    setCollection(collectionData);
                    setCollectionUpdateForm({
                        ...collectionData,
                        filterBooksDTO: undefined,
                    })
                    sessionMethods.api.getSmartCollection(collectionIdAsLong).then(async (smartCollectionData) => {
                        console.log("smartCollection:", smartCollectionData);
                        setSmartCollection(smartCollectionData);
                        const filterOfSmartCollection = await sessionMethods.api.getSmartCollectionFilter(collectionIdAsLong);
                        console.log("filterOfSmartCollection:", filterOfSmartCollection);
                        const smartCollection = {
                            ...smartCollectionData,
                            filterBooksDTO: filterOfSmartCollection
                        };
                        setSmartCollection(smartCollection);
                        setCollectionUpdateForm(smartCollection)
                    }).catch(() => {

                    })
                }).catch(() => {
                    navigate(ROUTES.collections.url)
                })

           /* try {} catch (error) {
                console.error("Error fetching collection data:", error);
                !collection && !smartCollection  && navigate(ROUTES.collections.url)
            }*/
        }

        fetchData().then();
    }

    useEffect(() => {
        const fetchData = async () => {
            const smartFilter = smartCollection?.filterBooksDTO;
            let filter: FilterBooksDTO | undefined;

            if (smartFilter) {
                filter = smartFilter;
                localStorage.setItem(`collectionFilter_${collectionId}`, JSON.stringify(filter));
            } else {
                const saved = localStorage.getItem(`collectionFilter_${collectionId}`);
                filter = saved ? JSON.parse(saved) : undefined;
            }

            if (filter) {
                const books = await bookFetcher({offset: 0, limit: 25}, filter);
                await onRefreshFilter(books, filter);
            } else {
                getMoreBooks().then();
            }
        };

        fetchData().then();
    }, [collectionId, smartCollection]);


    useEffect(() => {
        const handleScroll = () => {
            console.log("scrolling !");
            if (scrollContainerRef.current && !bookLoading) {
                const {scrollTop, scrollHeight, clientHeight} = scrollContainerRef.current;
                if (scrollTop + clientHeight >= scrollHeight - 5 && !bookLoading && scroller.canFetchMore()) {
                    getMoreBooks().then();
                }
            }
        };

        const scrollContainer = scrollContainerRef.current;
        if (scrollContainer) {
            scrollContainer.addEventListener('scroll', handleScroll);
        }

        return () => {
            if (scrollContainer) {
                scrollContainer.removeEventListener('scroll', handleScroll);
            }
        };
    }, [bookLoading, collectionId, scroller]);

    const owner = collection?.owner;
    const iAmOwner = sessionMethods.user?.id === owner?.id;
    const scrollContainerRef = useRef<HTMLDivElement>(null);
    const commentableId = collection?.commentableId;
    const followableId = collection?.followableId;

    //const [selectedBook, setSelectedBook] = useState<BookInfo | undefined>(undefined);

    function removeBook(book: BookInfo) {
        sessionMethods.api.removeBookFromCollection(Number(collectionId), book.id).then(() => {
            setScroller(new DataPageScroller<BookInfo>(
                scroller.data.filter(b => b.id !== book.id),
                scroller.numberResultMax,
                scroller.offset,
                scroller.limit,
                bookFetcher
            ));

            if (sessionMethods.session.loggedSession?.collections !== undefined) {
                sessionMethods.setCollections(sessionMethods.session.loggedSession.collections.map((c: UserCollectionInfo) => {
                    if (c.id == collection?.id) {
                        return {
                            ...c,
                            bookCount: Math.max(0, c.bookCount - 1)
                        } as UserCollectionInfo;
                    }
                    return c;
                }));
                setCollection({
                    ...collection!,
                    bookCount: Math.max(0, collection!.bookCount - 1)
                });
            }
        });
    }

    function updateCollection() {
        if (collectionUpdateForm?.id === undefined) return;
        sessionMethods.api.updateCollection(collectionUpdateForm?.id, collectionUpdateForm)
            .then(() => {
            setEditMode(false);
                fetchCollection();
        })
    }

    function deleteCollection() {
        if (collection?.id === undefined) return;
        sessionMethods.api.deleteCollection(collection.id)
            .then(() => {
                if(sessionMethods.session.loggedSession?.collections) {
                    const collections = sessionMethods.session.loggedSession?.collections.filter(c => c.id !== collection?.id)
                    sessionMethods.setCollections(collections)
                }
                if(sessionMethods.session.loggedSession?.user?.id === collection?.owner?.id) {
                    navigate(ROUTES.profile.url)
                } else {
                    navigate(ROUTES.collections.url)
                }
            })
    }

    return <>
        <div className="flex justify-center min-h-screen bg-gray-100">
            <div className="w-3/4 p-4 bg-white rounded-lg shadow-lg h-full">

                <div className="flex items-center justify-between">

                    {/* Collection title */}
                    <div className="flex items-center gap-4">
                        <div className="flex items-center gap-4">
                            {!editMode && <>
                                <h2 className="font-semibold">{collection?.title}</h2>
                            </>}

                            {editMode && <>
                                <input
                                    type="text"
                                    className={`border-2 border-gray-200 p-1 rounded-md ${editMode ? 'block' : 'hidden'}`}
                                    size={collectionUpdateForm?.title?.length}
                                    value={collectionUpdateForm?.title}
                                    onChange={(e) => setCollectionUpdateForm({
                                        ...collectionUpdateForm!,
                                        title: e.target.value
                                    })}
                                />
                            </>}

                            {/* Book count */}
                            <p className={`inline-block px-2 py-1 text-sm rounded-md bg-green-100 text-green-800`}>
                                <GiBlackBook className="inline-block"/>&nbsp;{collection?.bookCount}
                            </p>

                            {/* Visibility */}
                            {iAmOwner && <>
                                {/* Smart collection */}
                                {smartCollection && <p
                                    className={`inline-block px-2 py-1 text-sm rounded-md bg-blue-200 text-blue-800`}>
                                    <LuBrain className="inline-block"/>&nbsp;Smart collection
                                </p>
                                }

                                {!editMode && <>
                                    <p
                                        className={`inline-block px-2 py-1 text-sm rounded-md ${
                                            collection?.visibility ? "bg-green-200 text-green-800" : "bg-red-200 text-red-800"
                                        }`}
                                    >
                                        {collection?.visibility ? "Public" : "Private"}
                                    </p>
                                </>}

                                {editMode && <>
                                    <p
                                        className={`inline-block px-2 py-1 text-sm rounded-md
                                        hover:cursor-pointer
                                        hover:border-1 hover:border-black-200
                                         ${collectionUpdateForm?.visibility ? "bg-green-200 text-green-800" : "bg-red-200 text-red-800"}`}
                                        onClick={() =>
                                            setCollectionUpdateForm({
                                                ...collectionUpdateForm!,
                                                visibility: !collectionUpdateForm?.visibility
                                            })
                                        }
                                    >
                                        {collectionUpdateForm?.visibility ? "Public" : "Private"}
                                    </p>
                                    <div
                                        className={`inline-block p-2 text-sm rounded-md bg-gray-100 text-gray-800 hover:bg-gray-200 hover:cursor-pointer
                                        hover:border-1 hover:border-black-200
                                    `}
                                        onClick={() => setEditMode(!editMode)}
                                    >
                                        <AiOutlineRollback />
                                    </div>
                                    <div
                                        className={`inline-block p-2 text-sm rounded-md bg-gray-100 text-gray-800 hover:bg-gray-200 hover:cursor-pointer
                                       hover:border-1 hover:border-black-200
                                    `}
                                        onClick={updateCollection}
                                    >
                                        <IoMdCheckmarkCircle />
                                    </div>
                                </>}

                                {!editMode  && <>
                                    <div
                                        className={`inline-block p-2 text-sm rounded-md bg-gray-100 text-gray-800 hover:bg-gray-200 hover:cursor-pointer
                                        ${editMode ? 'border-2 border-black-200' : ''}
                                    `}
                                        onClick={() => setEditMode(!editMode)}
                                    >
                                        <MdModeEdit />
                                    </div>
                                </>}
                            </>}
                            {(iAmOwner || sessionMethods.loggedAndAdmin) && !editMode && <>
                                <div
                                    className={`inline-block p-2 text-sm rounded-md 
                                        ${sessionMethods.loggedAndAdmin ? "bg-orange-100 text-gray-800 hover:bg-orange-200" : 'bg-gray-100 text-gray-800 hover:bg-gray-200'}
                                         hover:cursor-pointer`}
                                    onClick={() => {
                                        setEditMode(false)
                                        setDeleteModal(true)
                                    }}>
                                    <FaTrash />
                                </div>
                                {deleteModal && <>
                                    <ConfirmationModal
                                        title={"Collection deletion"}
                                        message={"You are about to delete this collection. Continue ?"}
                                        onConfirm={deleteCollection}
                                        onCancel={() => setDeleteModal(false)}
                                    />
                                </>}
                            </>}
                        </div>

                    </div>

                    {/* Owner */}
                    <div className="flex items-center gap-3 hover:cursor-pointer"
                         onClick={() => {
                             if (owner?.id === undefined) return;
                             navigate(ROUTES.publicProfile.url.replace(':userId', owner?.id.toString()))
                         }}
                    >
                        <span className="flex items-center">{owner?.username}</span>
                        <img src={owner?.imageUrl} alt="avatar" className="rounded-full w-8 h-8"/>
                    </div>
                </div>

                <div className="">
                    <h1 className="text-orange-500 font-semibold border-b-2 border-orange-500 pb-2 mb-4"></h1>
                </div>

                {/* Follow */}
                {!editMode && <>
                    <div className="flex items-center justify-between">
                        <div></div>
                        <div className="flex items-center gap-4">
                            <FollowComponent followableId={followableId!}/>
                        </div>
                    </div>
                </>}


                {/* Collection description */}
                {!editMode && <>
                    <div className="">
                        <div className="text-gray-600 border-b-6 border-gray-200 p-2 border-r-0 border-l-0">
                            {collection?.description}
                        </div>
                    </div>
                </>}

                {editMode && <>
                    <div className="w-full">
                        <textarea
                            className={`border-2 border-gray-200 p-1 rounded-md w-full ${editMode ? 'block' : 'hidden'}`}
                            value={collectionUpdateForm?.description}
                            onChange={(e) => setCollectionUpdateForm({
                                ...collectionUpdateForm!,
                                description: e.target.value
                            })}
                        />
                    </div>
                </>}


                {/* Filter */}
                {!editMode && <>
                    <BookFilter
                        collectionId={Number(collectionId)}
                        sessionMethod={sessionMethods}
                        onRefresh={(books, filter) => onRefreshFilter(books, filter)}
                        localFilter={smartCollection?.filterBooksDTO ?? (() => {
                            const saved = localStorage.getItem(`collectionFilter_${collectionId}`);
                            return saved ? JSON.parse(saved) : undefined;
                        })()}
                    />
                </>}


                {/* Books */}
                {!editMode && <>
                    <div className="bg-gray-100 p-4 border-b-6 border-gray-200 overflow-y-auto h-[50vh]"
                         ref={scrollContainerRef}>
                        <div className="flex justify-between">
                            <h1 className="text-orange-500 font-semibold">Books ({scroller.numberResultMax})</h1>
                        </div>
                        <div className="p-1">
                            {scroller.data.map((book) => (
                                <BookPreviewComponent
                                    key={book.id}
                                    book={book}
                                    removeBook={removeBook}
                                    iAmOwner={iAmOwner}/>
                            ))}
                            {bookLoading && <Loader></Loader>}
                        </div>
                    </div>

                    {/* Comments */}
                    {commentableId && <CommentSection
                        getComments={(page: PageRequest) =>
                            sessionMethods.api.getComments(commentableId!, page)}
                        commentableId={commentableId!}></CommentSection>
                    }
                </>}
            </div>
        </div>
    </>
};

export default CollectionDetail;


/*


 */