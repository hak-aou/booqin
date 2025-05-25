import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {useNavigate} from "react-router-dom";
import {ROUTES} from "../../routes/routes.ts";
import {PageRequest} from "../../model/common.ts";
import FollowButton from "../../component/followers/FollowButton.tsx";
import {MdOutlineSettings} from "react-icons/md";
import {FaPlus} from "react-icons/fa";
import {useState} from "react";
import CollectionCreationFormModal from "../collections/CollectionCrationFormModal.tsx";
import {UserCollectionInfo} from "../../model/userCollectionInfo.ts";

export default function PrivateProfile() {
    const sessionMethods = useSessionMethods();
    const user = sessionMethods.user
    const navigate = useNavigate()
    const myUserId = sessionMethods.user?.id;
    const myFollowableId = sessionMethods.user?.followableId;
    const [openCollectionForm, setOpenCollectionForm] = useState(false);
    const [collectionSearch, setCollectionSearch] = useState<string>("");

    const getUserFollowers = (page: PageRequest) =>
        sessionMethods.api.getFollowers(myFollowableId!, page)

    const getUserFollowing = (page: PageRequest) =>
        sessionMethods.api.getUsersFollowedByAGivenUser(myUserId!, page)

    return <>
        <div className="flex justify-center min-h-screen bg-gray-100">
            <div className="w-3/4 p-4 bg-white rounded-lg shadow-lg h-full">
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                        <div className="flex items-center gap-4">
                            <img src={user?.imageUrl} alt="avatar" className="rounded-full w-20 h-20"/>
                            <h2>{sessionMethods.user?.username}</h2>
                        </div>
                    </div>
                    <div className="flex items-center gap-3">
                        <FollowButton
                            getUsersRequest={getUserFollowers}
                            className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md
                            hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 hover:cursor-pointer"
                            label={(count: number) => `${count} followers`}
                        />
                        <FollowButton
                            getUsersRequest={getUserFollowing}
                            className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md
                            hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 hover:cursor-pointer"
                            label={(count: number) => `${count} following`}
                        />
                        {/*Display a gear for paremeters*/}
                        <MdOutlineSettings
                            className="text-2xl text-gray-500 hover:cursor-pointer"
                            onClick={() => navigate(ROUTES.account.url)}
                        />
                    </div>
                </div>

                {/*<div className="w-[100%] mx-auto p-4 bg-white rounded-lg shadow mt-5">
                    <p>this is a section</p>

                    <a  className="text-blue-500 hover:underline hover:cursor-pointer"
                        onClick={() => navigate(ROUTES.publicProfile.url.replace(':userId', myUserId!))}>
                        See how other users see your profile
                    </a>
                </div>*/}

                <div className="w-[100%] mx-auto p-4 bg-white rounded-lg shadow mt-5 p-4">
                    <p>
                        My collections <span className="text-gray-400 text-sm">
                            ({sessionMethods.session.loggedSession?.collections?.length})
                        </span>
                    </p>
                    <input
                        type="text"
                        placeholder="Search collections"
                        className="w-full p-2 mt-2 border border-gray-300 rounded-md"
                        value={collectionSearch}
                        onChange={(e) => setCollectionSearch(e.target.value)}
                    />
                    <div className="flex gap-4 mt-2 overflow-x-scroll scrollbar-hide p-4">
                        <div className="rounded-lg p-2
                            min-w-[200px] max-w-[200px] bg-white
                            shadow-sm hover:scale-102 hover:cursor-pointer border-dashed
                            text-gray-400 text-align-center
                            border-2 border-gray-300 flex flex-col items-center justify-center"
                             onClick={() => setOpenCollectionForm(true)}
                        >
                            <FaPlus className="text-2xl"/>
                            <h1 className="text-lg text-xs font-semibold">new collection</h1>
                        </div>
                        {sessionMethods.session.loggedSession?.collections
                            ?.filter((c) => collectionSearch.trim() === '' || c.title.toLowerCase().includes(collectionSearch.toLowerCase()))
                            ?.sort((a, b) => a.title.localeCompare(b.title))
                            .map((collection: UserCollectionInfo) => (
                                <div key={collection.id} className="rounded-lg p-2
                                            min-w-[200px] max-w-[200px] bg-white
                                            shadow-sm hover:scale-102 hover:cursor-pointer "
                                     onClick={() => navigate(ROUTES.collectionDetail.url.replace(':collectionId', collection.id.toString()))}>
                                    <h1 className="text-lg font-semibold">
                                        {collection.title}
                                    </h1>
                                    <p className="text-sm">{collection.description.slice(0, 25)}{collection.description.length > 25 ? "..." : ""}
                                    </p>
                                    <p
                                        className={`inline-block px-2 py-1 text-sm rounded-md mb-4 ${
                                            collection.visibility ? "bg-green-200 text-green-800" : "bg-red-200 text-red-800"
                                        }`}
                                    >
                                        {collection.visibility ? "Public" : "Private"}
                                    </p>
                                    <p>
                                    <span className="text-gray-400 text-sm">
                                        {collection.bookCount} book{collection.bookCount > 1 ? "s" : ""}
                                    </span>
                                    </p>
                                </div>
                            ))
                        }
                    </div>
                </div>

                {/* Account details */}
                {/*<div className="p-4">
                    <h1 className="text-orange-500 font-semibold"></h1>
                    <div style={{textAlign: "left"}}>
                        this is aligned in the main content but not in a section
                    </div>
                </div>
                <div className="p-4">

                </div>*/}
                {openCollectionForm &&
                    <CollectionCreationFormModal onClose={() => setOpenCollectionForm(false)}/>
                }
            </div>
        </div>
    </>
}



