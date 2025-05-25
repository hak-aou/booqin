import {UserPublicInfo} from "./userPublicInfo.ts";

export interface UserCollectionInfo {
    id: number;
    commentableId: string;
    followableId: string;
    title: string;
    description: string;
    visibility: boolean;
    bookCount: number;
    owner: UserPublicInfo;
}

export interface CollectionCreation {
    title: string;
    description: string;
    visibility: boolean;
}
