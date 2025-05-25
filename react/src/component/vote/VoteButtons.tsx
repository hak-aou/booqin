import {BiSolidDownvote, BiSolidUpvote} from "react-icons/bi";
import {HasVoteDTO, VoteType} from "../../model/vote.ts";
import {useSession} from "../../hooks/session/sessionContext.tsx";

function SmallVoteButtons({
    handleVote,
    voteValue,
    hasVote
}: {
    handleVote: (voteType: any) => Promise<void>;
    voteValue: number,
    hasVote: HasVoteDTO
                          }) {

    const session = useSession();

    return (
        <div className="flex items-center space-x-1">
            {/* Upvote */}
            <button
                className={`flex items-center space-x-1 ${session.isLogged ? 'hover:text-blue-500 cursor-pointer' : ''} ${hasVote.hasVoted && hasVote.voteType === VoteType.UPVOTE ? 'text-blue-500' : ''}`}
                onClick={() => session.isLogged && handleVote(VoteType.UPVOTE)}>
                <BiSolidUpvote className="w-4 h-4"/>
            </button>

            {/* Value */}
            <span className=""> {voteValue} </span>

            {/* Downvote */}
            <button
                className={`flex items-center space-x-1 ${session.isLogged ? 'hover:text-red-500 cursor-pointer' : ''} ${hasVote.hasVoted && hasVote.voteType === VoteType.DOWNVOTE ? 'text-red-500' : ''}`}
                onClick={() => session.isLogged && handleVote(VoteType.DOWNVOTE)}>
                <BiSolidDownvote className="w-4 h-4"/>
            </button>
        </div>
    )
}

function BigVoteButtons({
                              handleVote,
                              voteValue,
                              hasVote
                          }: {
    handleVote: (voteType: any) => Promise<void>;
    voteValue: number,
    hasVote: HasVoteDTO
}) {
    const session = useSession();
    return (
        <>
            <div
                className="inline-flex items-center px-1 py-1 text-sm font-medium text-center border rounded-full bg-gray-100 ">
                <div className="flex items-center space-x-2">
                    {/* Upvote */}
                    <button className={`p-2 rounded-full bg-white  ${session.isLogged ? 'hover:cursor-pointer hover:bg-gray-300 active:bg-gray-500' : ''}`}
                            onClick={() => session.isLogged && handleVote(VoteType.UPVOTE)}>
                        <svg fill={hasVote.hasVoted && hasVote.voteType == VoteType.UPVOTE ? 'red' : 'black'} height="16" icon-name="downvote-outline" viewBox="0 0 20 20" width="16"
                             xmlns="http://www.w3.org/2000/svg">
                            <path
                                d="M10 19c-.072 0-.145 0-.218-.006A4.1 4.1 0 0 1 6 14.816V11H2.862a1.751 1.751 0 0 1-1.234-2.993L9.41.28a.836.836 0 0 1 1.18 0l7.782 7.727A1.751 1.751 0 0 1 17.139 11H14v3.882a4.134 4.134 0 0 1-.854 2.592A3.99 3.99 0 0 1 10 19Zm0-17.193L2.685 9.071a.251.251 0 0 0 .177.429H7.5v5.316A2.63 2.63 0 0 0 9.864 17.5a2.441 2.441 0 0 0 1.856-.682A2.478 2.478 0 0 0 12.5 15V9.5h4.639a.25.25 0 0 0 .176-.429L10 1.807Z"></path>
                        </svg>
                    </button>

                    {/* Vote value */}
                    <span className="text-2xl font-bold"> {voteValue} </span>

                    {/* Downvote */}
                    <button className={`p-2 rounded-full bg-white  ${session.isLogged ? 'hover:cursor-pointer hover:bg-gray-300 active:bg-gray-500' : ''} ${hasVote ? 'bg-red-500' : ''}`}
                            onClick={() => session.isLogged && handleVote(VoteType.DOWNVOTE)}>
                        <svg fill={hasVote.hasVoted && hasVote.voteType == VoteType.DOWNVOTE  ? 'red' : 'black'} height="16" icon-name="downvote-outline" viewBox="0 0 20 20" width="16"
                             xmlns="http://www.w3.org/2000/svg">
                            <path
                                d="M10 1c.072 0 .145 0 .218.006A4.1 4.1 0 0 1 14 5.184V9h3.138a1.751 1.751 0 0 1 1.234 2.993L10.59 19.72a.836.836 0 0 1-1.18 0l-7.782-7.727A1.751 1.751 0 0 1 2.861 9H6V5.118a4.134 4.134 0 0 1 .854-2.592A3.99 3.99 0 0 1 10 1Zm0 17.193 7.315-7.264a.251.251 0 0 0-.177-.429H12.5V5.184A2.631 2.631 0 0 0 10.136 2.5a2.441 2.441 0 0 0-1.856.682A2.478 2.478 0 0 0 7.5 5v5.5H2.861a.251.251 0 0 0-.176.429L10 18.193Z"></path>
                        </svg>
                    </button>

                </div>
            </div>
        </>
    );

}


export {SmallVoteButtons, BigVoteButtons};
