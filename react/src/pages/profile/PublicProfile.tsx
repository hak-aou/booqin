import {useEffect, useState} from "react";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {useNavigate, useParams} from "react-router-dom";
import {FollowRelationship} from "../../model/followers.ts";
import {UserPublicInfo} from "../../model/userPublicInfo.ts";

export function PublicProfilePreview({publicProfile, withNavigate = false}: { publicProfile: UserPublicInfo, withNavigate: boolean }) {
    const navigate = useNavigate();
    return (
        <div
            onClick={() => navigate(ROUTES.publicProfile.url.replace(":userId", publicProfile?.id))}
            className={`flex items-center justify-between py-1 ${withNavigate ? 'hover:bg-gray-200 hover:cursor-pointer': ''} p-2 rounded-lg`}>
            <div className="flex items-center gap-4">
                <img src={publicProfile?.imageUrl} alt="avatar" className="rounded-full w-20 h-20"/>
                <h2 className="text-xl font-semibold">{publicProfile?.username}</h2>
            </div>
            <div className="flex items-center gap-3">
                {publicProfile && <UserFollowSection profile={publicProfile}/>}
            </div>
        </div>
    );
}

import UserFollowSection from "../../component/followers/UserFollowSection.tsx";
import {ROUTES} from "../../routes/routes.ts";

function PublicProfile() {
    const sessionMethods = useSessionMethods();
    const { userId } = useParams<{ userId: string }>();
    const [publicProfile, setPublicProfile] = useState<UserPublicInfo|undefined>(undefined);
    const [relationship, setRelationship] = useState<FollowRelationship>({following: false, followedAt: ""});

    useEffect(() => {
        // get public profile
        sessionMethods.api.getPublicProfile(userId!).then((user: UserPublicInfo) => {
            setPublicProfile(user);
        });
        // get relationship
        if(publicProfile?.followableId !== undefined) {
            sessionMethods.api.getRelationship(publicProfile?.followableId!).then((relationship: FollowRelationship) => {
                setRelationship(relationship);
            });
        }
    }, [userId, relationship.following, publicProfile?.followableId]);

    return <>
        <div className="flex justify-center min-h-screen bg-gray-100">
            <div className="w-3/4 p-4 bg-white rounded-lg shadow-lg h-full">
                <PublicProfilePreview publicProfile={publicProfile!!} withNavigate={false}/>
            </div>
        </div>
    </>
}

export default PublicProfile;