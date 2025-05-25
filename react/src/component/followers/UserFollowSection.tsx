import {UserPublicInfo} from "../../model/userPublicInfo.ts";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {PageRequest} from "../../model/common.ts";
import FollowComponent from "./FollowComponent.tsx";

interface UserFollowSectionProps {
    profile: UserPublicInfo;
}

export default function UserFollowSection({profile}: UserFollowSectionProps) {
    const sessionMethods = useSessionMethods();
    return <>
        <FollowComponent
            userId={profile?.id}
            followableId={profile?.followableId!}
            follow={() => {
                if(profile?.followableId) {
                    return sessionMethods.api.followUser(profile?.followableId!).then()
                }
                return Promise.resolve();
            }}
            fetchFollowing={(page: PageRequest) =>
                sessionMethods.api.getUsersFollowedByAGivenUser(profile?.id!, page)}
        ></FollowComponent>
    </>
}