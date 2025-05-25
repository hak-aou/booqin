import {useEffect, useRef, useState} from "react";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {useNavigate} from "react-router-dom";
import {ROUTES} from "../../routes/routes.ts";
import {UserCollectionInfo} from "../../model/userCollectionInfo.ts";
import {DataPageScroller} from "../../model/common.ts";
import Loader from "../../component/Loader.tsx";

function getPastelColor(bookCount: number): string {
    const maxBooks = 600; // Define the maximum number of books for the color scale
    const percentage = Math.min(bookCount / maxBooks, 1);

    const lightGreen = { r: 240, g: 255, b: 240 }; // Very light green
    const maxGreen = { r: 176, g: 214, b: 189 }; // #b0d6bd
    const alpha = 0.4;

    const r = Math.round(lightGreen.r + percentage * (maxGreen.r - lightGreen.r));
    const g = Math.round(lightGreen.g + percentage * (maxGreen.g - lightGreen.g));
    const b = Math.round(lightGreen.b + percentage * (maxGreen.b - lightGreen.b));

    return `rgb(${r}, ${g}, ${b}, ${alpha})`;
}

export function CollectionPreviewComponent({collection}: { collection: UserCollectionInfo }) {
    const navigate = useNavigate();
    const pastelColor = getPastelColor(collection.bookCount);

    return (
        <div
            key={collection.id}
            className="cursor-pointer shadow-lg rounded-xl p-4
                                    hover:bg-gray-100 transition duration-200
                                    hover:scale-105 transform
                                    "
            style={{ background: pastelColor }}
            onClick={() => navigate(ROUTES.collectionDetail.url.replace(':collectionId', "" + collection.id))}
        >
            <div className="flex flex-col justify-between items-center h-[12vh]">
                <div className="w-full">
                    <h2 className="text-lg font-normal text-center">{collection.title}</h2>
                    <p className="text-gray-600 text-center text-xs mt-2">{collection.description}</p>
                </div>
            </div>
        </div>
    )
}


export default function Collections() {
    const session = useSessionMethods();
    const [loading, setLoading] = useState(true);
    const scrollContainerRef = useRef<HTMLDivElement>(null);
    const [scroller, setScroller] = useState<DataPageScroller<UserCollectionInfo>>(
        new DataPageScroller<UserCollectionInfo>([], 0, 0, 20, session.api.getAllPublicCollection)
    );

    useEffect(() => {
        getMoreCollections().then();
    }, []);

    useEffect(() => {
        const handleScroll = () => {
            if (scrollContainerRef.current && !loading) {
                const { scrollTop, scrollHeight, clientHeight } = scrollContainerRef.current;
                if (scrollTop + clientHeight >= scrollHeight - 5 && !loading) {
                    getMoreCollections().then();
                    console.log("scroll, remaining: ", scroller)
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


    async function getMoreCollections() {
        setLoading(true);
        try {
            const newScroller = await scroller.fetchMoreData();
            setScroller(newScroller);
        } finally {
            setLoading(false);
        }
    }

    return <>
        <div className="flex justify-center ">
            <div className="w-4/4 p-4 bg-white rounded-lg shadow-lg">
                <div className="p-4">
                    <div className="max-w- mt-8">
                        <h1 className="text-2xl font-bold text-center mb-12">
                            Collections
                            <span className="text-gray-500 text-sm ml-2">
                                {scroller.numberResultMax > 0 && (scroller.numberResultMax)}
                            </span>
                        </h1>
                        {scroller.empty() && <div className="text-center text-gray-500">No collections available.</div>}

                        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 h-[58vh] overflow-hidden overflow-y-auto p-3" ref={scrollContainerRef}>
                            {scroller.data.map((collection) => (
                                <CollectionPreviewComponent key={collection.id} collection={collection}/>
                            ))}
                        </div>
                        {loading && <div><Loader/></div>}
                    </div>
                </div>
            </div>
        </div>
    </>
}