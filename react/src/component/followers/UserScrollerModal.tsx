import {useEffect, useRef, useState} from "react";
import {useNavigate} from "react-router-dom";
import {useSession} from "../../hooks/session/sessionContext.tsx";
import {DataPageScroller, PageRequest, PaginatedResult} from "../../model/common.ts";
import {UserPublicInfo} from "../../model/userPublicInfo.ts";

interface FollowersModalProps {
    getFollowerRequest: (page: PageRequest) => Promise<PaginatedResult<UserPublicInfo>>;
    close: () => void;
}

function UserScrollerModal({getFollowerRequest, close}: FollowersModalProps) {
    const session = useSession();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const scrollContainerRef = useRef<HTMLDivElement>(null);
    const [scroller, setScroller] = useState<DataPageScroller<UserPublicInfo>>(
        new DataPageScroller<UserPublicInfo>([], 0, 0, 10, getFollowerRequest)
    );

    useEffect(() => {
        // getMyFollowers
        getMoreFollowers().then();
    }, []);

    useEffect(() => {
        const handleScroll = () => {
            if (scrollContainerRef.current && !loading) {
                const { scrollTop, scrollHeight, clientHeight } = scrollContainerRef.current;
                if (scrollTop + clientHeight >= scrollHeight - 5 && !loading) {
                    getMoreFollowers().then();
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
    }, [loading]);

    async function getMoreFollowers() {
        setLoading(true);
        try {
            const newScroller = await scroller.fetchMoreData();
            setScroller(newScroller);
        } finally {
            setLoading(false);
        }
    }

    return <>
        <div className="fixed z-10 inset-0 overflow-y-auto">
            <div className="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
                <div className="fixed inset-0 transition-opacity" aria-hidden="true">
                    <div className="absolute inset-0 bg-gray-500 opacity-75"/>
                </div>
                <span className="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
                <div
                    className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full"
                >
                    <div className="bg-white p-4" ref={scrollContainerRef} style={{ maxHeight: '400px', overflowY: 'auto' }}>
                        <div className="flex justify-between">
                            <h1 className="text-orange-500 font-semibold">Followers</h1>
                            <button
                                className="bg-red-500 hover:bg-red-400 text-white font-bold py-2 px-4 border-b-4 border-red-700 hover:border-red-500 rounded hover:cursor-pointer"
                                onClick={close}
                            >
                                Close
                            </button>
                        </div>
                        <div style={{textAlign: "left"}}>
                            <ul>
                                {scroller.data
                                    .map((follower) => {
                                        return <li key={follower.id}>
                                            <div
                                                onClick={() => {
                                                    if(session.loggedSession?.user?.id === follower.id) {
                                                        // navigate to my private profile
                                                        navigate(`/profile`)
                                                    } else {
                                                        navigate(`/profile/${follower.id}`)
                                                    }
                                                    close()
                                                }}
                                                className="flex items-center gap-4 hover:bg-gray-100 p-2 hover:cursor-pointer">
                                                <img src={follower.imageUrl} alt="avatar"
                                                     className="rounded-full w-10 h-10"/>
                                                <h2>{follower.username}</h2>
                                            </div>
                                        </li>
                                    })}
                            </ul>
                        </div>

                        {loading && <div>Loading...</div>}
                        {scroller.canFetchMore() &&
                            <div>
                                and {scroller.remainingToFetch()} others
                            </div>
                        }
                    </div>

                </div>
            </div>
        </div>
    </>
}

export default UserScrollerModal;