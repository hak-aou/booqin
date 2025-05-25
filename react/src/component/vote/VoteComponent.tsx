// https://flowbite.com/docs/components/card/
// svg from reddit
import React, {useEffect, useState} from "react";
import {HasVoteDTO, VoteType} from "../../model/vote.ts";
import {SessionMethods} from "../../hooks/session/sessionContext.tsx";


function voteManager(sessionMethods: SessionMethods, votableId: string) {
    const [voteValue, setVoteValue] = useState<number>(0);
    const [hasVote, setHasVote] = useState<HasVoteDTO>({
        hasVoted: false,
        voteType: null,
        votedAt: null,
    });

    const handleVote = async (voteType: VoteType) => {
        if (!sessionMethods.session.isLogged) {
            console.log("Must be logged in to vote");
            return;
        }

        if (hasVote.hasVoted && hasVote.voteType == voteType) { // Retirer son vote
            await sessionMethods.api.unvote(votableId);
            await fetchVoteValue();
            await fetchHasVote();
            return;
        } else if (hasVote.hasVoted && hasVote.voteType != voteType) { // Change le vote
            await sessionMethods.api.unvote(votableId);
        }

        switch (voteType) {
            case VoteType.UPVOTE:
                await sessionMethods.api.upvote(votableId);
                break;
            case VoteType.DOWNVOTE:
                await sessionMethods.api.downvote(votableId);
                break;
            default:
                break;
        }

        await fetchVoteValue();
        await fetchHasVote();
    }

    const fetchVoteValue = async () => {
        try {
            const count = await sessionMethods.api.votevalue(votableId);
            setVoteValue(count);
        } catch (err) {
            console.error("Error fetching voteCount :", err);
        }
    };

    const fetchHasVote = async () => {
        if (!sessionMethods.session.isLogged) {
            return;
        }

        try {
            const hasVote = await sessionMethods.api.hasvoted(votableId);
            setHasVote(hasVote);
            console.log("Has vote :", hasVote);
        } catch (err) {
            console.error("Error fetching hasVote :", err);
        }
    }

    useEffect(() => {
        fetchVoteValue().then(() => console.log("Vote value :", voteValue));
        fetchHasVote().then(() => console.log("Has vote :", hasVote));
    }, [sessionMethods.isLogged]);

    return {handleVote, voteValue, hasVote};
}


function VoteComponent({
                           sessionMethods,
                           votableId,
                           ButtonComponent
                       }: {
    sessionMethods: SessionMethods;
    votableId: string;
    ButtonComponent: React.FC<{
        handleVote: (voteType: VoteType) => Promise<void>;
        voteValue: number;
        hasVote: HasVoteDTO;
    }>;
}) {
    const { handleVote, voteValue, hasVote } = voteManager(sessionMethods, votableId);

    return (
        <ButtonComponent
            handleVote={handleVote}
            voteValue={voteValue}
            hasVote={hasVote}
        />
    );
};



export default VoteComponent;