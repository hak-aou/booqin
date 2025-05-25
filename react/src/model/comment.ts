import {UserPublicInfo} from "./userPublicInfo.ts";

export interface CommentData {
    id: number;
    author: UserPublicInfo | null;
    content: string;

    votableId: string;

    parentComment: number;
    createdAt: any;
    repliesCount: number;
}

export interface Comment extends CommentData {
    replies: Comment[];
}

export interface ReplyComment {
    parentId: number;
    content: string;
}

export interface CommentCommentable {
    commentableId: string;
    content: string;
}