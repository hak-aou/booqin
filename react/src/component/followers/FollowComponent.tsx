import {PageRequest, PaginatedResult} from "../../model/common.ts";
import {UserPublicInfo} from "../../model/userPublicInfo.ts";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {useEffect, useState} from "react";
import {FollowRelationship} from "../../model/followers.ts";
import FollowButton from "./FollowButton.tsx";

interface FollowSectionProps {
    followableId: string;
    follow?: () => Promise<void>;
    fetchFollowing?: (page: PageRequest) => Promise<PaginatedResult<UserPublicInfo>>;
    userId?: string;
}

export default function FollowComponent(props : FollowSectionProps) {
    const followableId = props.followableId;
    const sessionMethods = useSessionMethods();
    const [relationship, setRelationship] = useState<FollowRelationship>({following: false, followedAt: ""});

    const getFollowers = (page: PageRequest) =>
        sessionMethods.api.getFollowers(followableId, page)

    useEffect(() => {
        // get relationship
        if(followableId !== undefined) {
            sessionMethods.api.getRelationship(followableId!).then((relationship: FollowRelationship) => {
                setRelationship(relationship);
            });
        }
    }, [relationship.following, followableId]);

    if(!sessionMethods.isLogged) {
        return <></>
    }

    return <>
        <FollowButton
            getUsersRequest={getFollowers}
            className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md
            hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 hover:cursor-pointer"
            label={(count: number) => `${count} followers`}
            dependsOn={[relationship.following]}
        />
        { props.userId && props.fetchFollowing &&
            <FollowButton
                getUsersRequest={props.fetchFollowing}
                className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md
                            hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 hover:cursor-pointer"
                label={(count: number) => `${count} following`}
                dependsOn={[relationship.following]}
            />
        }
        {sessionMethods.user?.id !== (props.userId || undefined) && <>
            {!relationship.following &&
                <button
                    className="inline-flex items-center px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent
                rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 hover:cursor-pointer"
                    onClick={() => {
                        const afterFollow = () => setRelationship({following: true, followedAt: new Date().toISOString()});
                        if(props.follow) {
                            props.follow().then(afterFollow);
                        }else {
                            followableId && sessionMethods.api.follow(followableId).then(afterFollow);
                        }
                    }}
                >
                    Follow
                </button>
            }

            {relationship.following &&
                <button
                    className="inline-flex items-center px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent
                rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 hover:cursor-pointer"
                    onClick={() => {
                        sessionMethods.api.unfollow(followableId!).then(() => {
                            setRelationship({following: false, followedAt: ""});
                        })
                    }}>
                    Unfollow
                </button>
            }
        </>}
    </>
}
