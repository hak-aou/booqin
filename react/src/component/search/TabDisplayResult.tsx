import {useEffect, useState} from "react";
import BookPreviewComponent from "../books/BookComponent.tsx";
import {DataPageScroller, PageRequest} from "../../model/common.ts";
import {SessionMethods} from "../../hooks/session/sessionContext.tsx";
import {BookInfo} from "../../model/book.ts";
import {UserPublicInfo} from "../../model/userPublicInfo.ts";
import {TabInformations} from "../../model/search.ts";
import {CollectionPreviewComponent} from "../../pages/collections/Collections.tsx";
import {PublicProfilePreview} from "../../pages/profile/PublicProfile.tsx";
import {UserCollectionInfo} from "../../model/userCollectionInfo.ts";

function renderContentTab(tabInfo: TabInformations,
                          collections: UserCollectionInfo[],
                          books: BookInfo[],
                          users: UserPublicInfo[]) {
    switch (tabInfo) {
        case TabInformations.COLLECTIONS:
            return <TabCollection
                collections={collections}/>;
        case TabInformations.BOOKS:
            return <TabBook
                books={books}/>;
        case TabInformations.USERS:
            return <TabUsers
                users={users}/>;
    }
}

function TabCollection({
                           collections
                       }:
                       {
                           collections: UserCollectionInfo[]
                       }) {
return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
        {collections.map((collection) => (
            <CollectionPreviewComponent
                collection={collection}
            />
        ))}
    </div>
);
}

function TabBook({
                     books
                 }:
                 {
                     books: BookInfo[]
                 }) {
    return (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
            {books.map((book) => (
                <div key={book.id}
                     className="bg-white shadow-lg rounded-xl p-4 hover:bg-gray-100 transition duration-200">
                    <BookPreviewComponent
                        book={book}
                    />
                </div>
            ))}
        </div>
    );
}

function TabUsers({
                      users
                  }:
                  {
                      users: UserPublicInfo[]
                  }) {
    return (
        <div className="p-2">
            {users.map((user) => (
                <PublicProfilePreview
                    withNavigate={true}
                    publicProfile={user}
                />
            ))}
        </div>
    );
}

// Source : https://flowbite.com/docs/components/tabs/
function TabDisplayResult(
    {
        sessionMethods,
        searchData,
    }: {
        sessionMethods: SessionMethods,
        searchData: string,
    }
) {
    const [activeTab, setActiveTab] = useState(TabInformations.COLLECTIONS);
    const isTabActive = "inline-block p-4 text-blue-600 border-b-2 border-blue-600 rounded-t-lg active dark:text-blue-500 dark:border-blue-500"
    const isTabNotActive = "inline-block p-4 border-b-2 border-transparent rounded-t-lg hover:text-gray-600 hover:border-gray-300 dark:hover:text-gray-300"

    const [collectionLoading, setCollectionLoading] = useState(false);
    const [bookLoading, setBookLoading] = useState(false);
    const [userLoading, setUserLoading] = useState(false);

    const collectionsFetcher = (page: PageRequest) =>
        sessionMethods.api.searchCollections(searchData, page);

    const booksFetcher = (page: PageRequest) =>
        sessionMethods.api.searchBooks(searchData, page);

    const usersFetcher = (page: PageRequest) =>
        sessionMethods.api.searchUsers(searchData, page);

    const [scrollerCollection, setScrollerCollection] = useState<DataPageScroller<UserCollectionInfo>>(
        new DataPageScroller<UserCollectionInfo>([], 0, 0, 25, collectionsFetcher)
    );

    const [scrollerBooks, setScrollerBooks] = useState<DataPageScroller<BookInfo>>(
        new DataPageScroller<BookInfo>([], 0, 0, 25, booksFetcher)
    );

    const [scrollerUsers, setScrollerUsers] = useState<DataPageScroller<UserPublicInfo>>(
        new DataPageScroller<UserPublicInfo>([], 0, 0, 25, usersFetcher)
    );

    async function getMoreCollections(scroller: DataPageScroller<UserCollectionInfo>) {
        try {
            setCollectionLoading(true);
            setScrollerCollection(await scroller.fetchMoreData());
        } finally {
            setCollectionLoading(false);
        }
    }

    async function getMoreBooks(scroller: DataPageScroller<BookInfo>) {
        try {
            setBookLoading(true);
            setScrollerBooks(await scroller.fetchMoreData());
        } finally {
            setBookLoading(false);
        }
    }

    async function getMoreUsers(scroller: DataPageScroller<UserPublicInfo>) {
        try {
            setUserLoading(true);
            setScrollerUsers(await scroller.fetchMoreData());
        } finally {
            setUserLoading(false);
        }
    }

    useEffect(() => {
        const newScrollerCollections = new DataPageScroller([], 0, 0, 25, collectionsFetcher);
        const newScrollerBooks = new DataPageScroller([], 0, 0, 25, booksFetcher);
        const newScrollerUsers = new DataPageScroller([], 0, 0, 25, usersFetcher);

        getMoreCollections(newScrollerCollections).then();
        getMoreBooks(newScrollerBooks).then();
        getMoreUsers(newScrollerUsers).then();
    }, [searchData]);

    useEffect(() => {
        const handleScroll = () => {
            console.log("scrolling !");
            if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 5) {
                switch (activeTab) {
                    case TabInformations.COLLECTIONS:
                        if (!collectionLoading) getMoreCollections(scrollerCollection).then();
                        break;
                    case TabInformations.BOOKS:
                        if (!bookLoading) getMoreBooks(scrollerBooks).then();
                        break;
                    case TabInformations.USERS:
                        if (!userLoading) getMoreUsers(scrollerUsers).then();
                        break;
                }
            }
        };

        window.addEventListener('scroll', handleScroll);

        return () => {
            window.removeEventListener('scroll', handleScroll);
        };
    }, [activeTab, collectionLoading, bookLoading, userLoading]);

    const collectionsTotalResult = scrollerCollection.numberResultMax;
    const booksTotalResult = scrollerBooks.numberResultMax;
    const usersTotalResult = scrollerUsers.numberResultMax;

    return (
        <div
            className="text-sm font-medium text-center text-gray-500 border-b border-gray-200 dark:text-gray-400 dark:border-gray-700">
            <ul className="flex flex-wrap -mb-px">

                {/* Tab Collection */}
                <li className="me-2 cursor-pointer">
                    <a
                        className={activeTab === TabInformations.COLLECTIONS ? isTabActive : isTabNotActive}
                        onClick={() => setActiveTab(TabInformations.COLLECTIONS)}>
                        Collections ({collectionsTotalResult})
                    </a>
                </li>

                {/* Tab Books */}
                <li className="me-2 cursor-pointer">
                    <a
                        className={activeTab === TabInformations.BOOKS ? isTabActive : isTabNotActive}
                        onClick={() => setActiveTab(TabInformations.BOOKS)}>
                        Books ({booksTotalResult})
                    </a>
                </li>

                {/* Tab Users */}
                <li className="me-2 cursor-pointer">
                    <a
                        className={activeTab === TabInformations.USERS ? isTabActive : isTabNotActive}
                        onClick={() => setActiveTab(TabInformations.USERS)}>
                        Users ({usersTotalResult})
                    </a>
                </li>
            </ul>

            {/* Tab content */}
            {(collectionLoading || bookLoading || userLoading) ? (
                <div className="flex flex-col mt-4 space-y-2 bg-gray-100 rounded-md p-2">
                    <div className="flex flex-col mt-4 space-y-2 bg-gray-100 rounded-md p-4">
                        <div className="bg-gray-300 rounded-md w-3/4 h-5 animate-pulse space-x-4"></div>
                        <div className="bg-gray-300 rounded-md w-4/5 h-5 animate-pulse space-x-4"></div>
                    </div>
                    <div className="flex flex-col mt-4 space-y-2 bg-gray-100 rounded-md p-4">
                        <div className="bg-gray-300 rounded-md w-3/4 h-5 animate-pulse space-x-4"></div>
                        <div className="bg-gray-300 rounded-md w-4/5 h-5 animate-pulse space-x-4"></div>
                    </div>
                    <div className="flex flex-col mt-4 space-y-2 bg-gray-100 rounded-md p-4">
                        <div className="bg-gray-300 rounded-md w-3/4 h-5 animate-pulse space-x-4"></div>
                        <div className="bg-gray-300 rounded-md w-4/5 h-5 animate-pulse space-x-4"></div>
                    </div>
                </div>
            ) : (
                <div className={"flex flex-col gap-4"}>
                    <div className="bg-gray-100 p-4 border-b-6 border-gray-200 overflow-y-auto h-full">
                        {renderContentTab(activeTab, scrollerCollection.data, scrollerBooks.data, scrollerUsers.data)}
                    </div>
                </div>
            )}
        </div>

    );
}

export default TabDisplayResult;