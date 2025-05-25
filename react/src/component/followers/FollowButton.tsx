import {PageRequest, PaginatedResult} from "../../model/common.ts";
import {UserPublicInfo} from "../../model/userPublicInfo.ts";
import {useEffect, useState} from "react";
import UserScrollerModal from "./UserScrollerModal.tsx";

interface FollowButtonProps {
    getUsersRequest: (page: PageRequest) => Promise<PaginatedResult<UserPublicInfo>>,
    className: string,
    label: (count: number) => string,
    dependsOn?: any;
}

function FollowButton(props: FollowButtonProps) {

    const [followingCount, setFollowingCount] = useState<number>(0);
    const [modalIsOpen, setModalIsOpen] = useState<boolean>(false);

    useEffect(() => {
        props.getUsersRequest({offset: 0, limit: 1})
            .then((response) => {
                setFollowingCount(response.totalResults)
            })
    }, [props.dependsOn]);

    return <>
        <button className={props.className} onClick={() => setModalIsOpen(!modalIsOpen)}>
            {props.label(followingCount)}
        </button>
        {/*Followers modal like instagram*/}
        {modalIsOpen &&
            <UserScrollerModal getFollowerRequest={props.getUsersRequest} close={() => setModalIsOpen(false)}/>
        }
    </>
}


export default FollowButton;


