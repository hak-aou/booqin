export enum VoteType {
    UPVOTE = "UPVOTE",
    DOWNVOTE = "DOWNVOTE",
}

export interface HasVoteDTO {
    hasVoted: boolean;
    voteType: VoteType | null;
    votedAt: Date | null;
}