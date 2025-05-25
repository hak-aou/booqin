import {useState} from "react";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {UserCollectionInfo} from "../../model/userCollectionInfo.ts";
import {FaBookmark} from "react-icons/fa";


interface AddToCollectionButtonProps {
    bookId: string;
    bookTitle: string;
}

interface CollectionPossessingTheBook {
    collection: UserCollectionInfo;
    hasBook: boolean;
}

export default function AddToCollectionButton(props: AddToCollectionButtonProps) {
    const sessionMethods = useSessionMethods();
    const myCollections = sessionMethods.session.loggedSession?.collections;
    const [collectionPossessingTheBook, setCollectionPossessingTheBook] = useState<CollectionPossessingTheBook[]>([]);
    const [collectionSelectionModalOpen, setCollectionSelectionModalOpen] = useState(false);
    const [search, setSearch] = useState('');

    if(!sessionMethods.session.isLogged) {
        return <></>
    }

    function getCollectionsContainingTheBook() {
        sessionMethods.api.whichCollection(props.bookId).then((collections: UserCollectionInfo[]) => {
            console.log(collections);
            setCollectionPossessingTheBook(myCollections!.map(collection => {
                return {
                    collection: collection,
                    hasBook: collections.some(c => c.id === collection.id)
                }}));
            setCollectionSelectionModalOpen(true);
        });
    }

    function addBookToCollection(collectionId: number) {
        sessionMethods.api.addBookToCollection(collectionId, props.bookId)
            .then(() => {
                setCollectionPossessingTheBook(collectionPossessingTheBook.map(c => {
                    if(c.collection.id === collectionId) {
                        return {
                            ...c,
                            hasBook: true
                        }
                    }
                    return c;
                }));
                if(sessionMethods.session.loggedSession?.collections) {
                    sessionMethods.setCollections(sessionMethods.session.loggedSession.collections.map(c => {
                        if (c.id === collectionId) {
                            return {
                                ...c,
                                bookCount: c.bookCount + 1
                            } as UserCollectionInfo;
                        }
                        return c;
                    }));
                }
            })
    }

    function removeBookFromCollection(collectionId: number) {
        sessionMethods.api.removeBookFromCollection(collectionId, props.bookId)
            .then(() => {
                setCollectionPossessingTheBook(collectionPossessingTheBook.map(c => {
                    if(c.collection.id === collectionId) {
                        return {
                            ...c,
                            hasBook: false
                        }
                    }
                    return c;
                }));
                if(sessionMethods.session.loggedSession?.collections) {
                    sessionMethods.setCollections(sessionMethods.session.loggedSession.collections.map(c => {
                        if (c.id === collectionId) {
                            console.log(c.bookCount);
                            return {
                                ...c,
                                bookCount: Math.max(0, c.bookCount - 1)
                            } as UserCollectionInfo;
                        }
                        return c;
                    }));
                }
            })
    }

    return <>
        <button
            className="p-2 rounded-lg hover:cursor-pointer group"
            onClick={(event) => {
                event.stopPropagation();
                getCollectionsContainingTheBook();
            }}
        >
            <svg stroke="currentColor" fill="currentColor" strokeWidth="1" viewBox="0 0 24 24"
                 className="text-2xl font-bold group-hover:text-teal-700 group-hover:font-extrabold  text-gray-300"
                 height="1em" width="1em" xmlns="http://www.w3.org/2000/svg"><g id="Bookmark_Plus"><g><path
                d="M17.6,21.938a1.482,1.482,0,0,1-1.011-.4l-4.251-3.9a.5.5,0,0,0-.678,0L7.41,21.538a1.5,1.5,0,0,1-2.517-1.1V4.563a2.5,2.5,0,0,1,2.5-2.5h9.214a2.5,2.5,0,0,1,2.5,2.5V20.435a1.483,1.483,0,0,1-.9,1.375A1.526,1.526,0,0,1,17.6,21.938ZM12,16.5a1.5,1.5,0,0,1,1.018.395L17.269,20.8a.5.5,0,0,0,.838-.368V4.563a1.5,1.5,0,0,0-1.5-1.5H7.393a1.5,1.5,0,0,0-1.5,1.5V20.435a.5.5,0,0,0,.839.368L10.983,16.9A1.5,1.5,0,0,1,12,16.5Z"></path><path
                d="M14,10.28H12.5v1.5a.5.5,0,0,1-1,0v-1.5H10a.5.5,0,0,1,0-1h1.5V7.78a.5.5,0,0,1,1,0v1.5H14A.5.5,0,0,1,14,10.28Z"></path></g></g>
            </svg>
        </button>
        {collectionSelectionModalOpen && <>
            <div className="fixed z-10 inset-0 overflow-y-auto">
                <div className="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
                    <div className="fixed inset-0 transition-opacity" aria-hidden="true">
                        <div className="absolute inset-0 bg-gray-500 opacity-75"/>
                    </div>
                    <span className="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
                    <div
                        className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full"
                    >
                        <div className="bg-white p-4" style={{ maxHeight: '600px'}}>
                            <div className="flex justify-between ">
                                <h1 className="text-orange-500 font-semibold">My collections</h1>
                                <button
                                    className="bg-red-500 hover:bg-red-400 text-white font-bold py-2 px-4 border-b-4 border-red-700 hover:border-red-500 rounded hover:cursor-pointer"
                                    onClick={() => setCollectionSelectionModalOpen(false)}
                                >
                                    Close
                                </button>
                            </div>
                            <div className="p-4">
                                <p className="text-gray-900 font-medium text-center">{props.bookTitle}</p>
                            </div>
                            {/*Dynamic search bar filtering collection title*/}
                            <div className="p-4">
                                <input type="text" placeholder="Search collection"
                                        onChange={(event) => setSearch(event.target.value)}
                                       className="w-full p-2 border border-gray-200 rounded-lg"/>
                            </div>
                            <div style={{textAlign: "left", overflowY: 'auto',maxHeight: '600px' }} className="p-4">
                                <ul>
                                    {collectionPossessingTheBook
                                        .filter((possesionState) =>
                                            search.trim() === '' ||
                                            possesionState.collection.title.toLowerCase().includes(search.toLowerCase()))
                                        .sort((a, b) =>
                                            a.collection.title.localeCompare(b.collection.title))
                                        .map((possesionState) => {
                                            return <>
                                                <li key={possesionState.collection.id}
                                                    className="group"
                                                    onClick={(event) => {
                                                        event.stopPropagation();
                                                        if(!possesionState.hasBook) {
                                                            addBookToCollection(possesionState.collection.id)
                                                        } else {
                                                            removeBookFromCollection(possesionState.collection.id)
                                                        }
                                                    }}
                                                >
                                                    <div className={`flex gap-4 mt-3 hover:cursor-pointer group 
                                                    ${possesionState.hasBook ?  'hover:bg-red-50' : 'hover:bg-green-50'}`}>
                                                        <div className="flex flex-col" >
                                                            <h2 className="text-md font-normal text-black ">
                                                                {possesionState.collection.title}</h2>
                                                        </div>
                                                        <div className="flex flex-col ml-auto"

                                                        >
                                                            <FaBookmark size={20} className={`text-gray-200 
                                                            ${possesionState.hasBook ? 'text-teal-600' : ''}
                                                            group-hover:text-teal-600`}/>
                                                        </div>
                                                    </div>
                                                </li>
                                            </>}
                                        )
                                    }
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>}
    </>
}