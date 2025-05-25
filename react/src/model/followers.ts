import {PageRequest} from "./common.ts";


export interface FollowersRequest {
    objectId: string
    pageRequest: PageRequest;
}

export interface FollowRelationship {
    following: boolean;
    followedAt: any;
}