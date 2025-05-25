import {useEffect, useState} from "react";
import {DataPageScroller, PageRequest, PaginatedResult} from "../../model/common.ts";
import {Comment, CommentData} from "../../model/comment.ts";
import RecursiveCommentContainer from "./RecursiveCommentContainer.tsx";
import Loader from "../Loader.tsx";
import ReplyForm from "./ReplyForm.tsx";
import {useSession} from "../../hooks/session/sessionContext.tsx";

interface CommentSectionProps {
    getComments: (page: PageRequest) => Promise<PaginatedResult<Comment>>;
    commentableId: string;
}

export function CommentSection(props: CommentSectionProps) {
    const [loading, setLoading] = useState(true);
    const [scroller, setScroller] = useState<DataPageScroller<Comment>>(
        new DataPageScroller<Comment>([], 0, 0, 10, props.getComments)
    );
    const session = useSession();

    useEffect(() => {
        getMoreComments().then();
    }, []);

    async function getMoreComments() {
        setLoading(true);
        try {
            const newScroller = await scroller.fetchMoreData();
            setScroller(newScroller);
        } finally {
            setLoading(false);
        }
    }

    function onSendComment(commentData: CommentData) {
        const newComment = {
            ...commentData,
            replies: [],

        } as Comment;
        const newScroller = new DataPageScroller<Comment>(
            [newComment, ...scroller.data],
            scroller.numberResultMax + 1,
            scroller.offset + 1,
            scroller.limit,
            props.getComments
        );
        setScroller(newScroller);
    }

    function adminDeleteCommentAsCommentable(comment: Comment) {
        const replies = scroller.data.filter(reply => reply.id !== comment.id)
            // find parent and set repliesCount to 0
            .map(reply => {
                if (reply.id === comment.parentComment) {
                    return {
                        ...reply,
                        repliesCount: 0,
                        replies: []
                    }
                }
                return reply;
            });
        setScroller(new DataPageScroller<Comment>(
            replies,
            scroller.numberResultMax - 1,
            0,
            10,
            props.getComments
        ));
    }

    return (
        <div className="max-w mx-auto p-2 bg-white rounded-lg  ">
            {/*Total comments*/}
            <div className="text-sm text-gray-500">
                {scroller.numberResultMax} comments
            </div>
        <div className="max-w mx-auto p-4 bg-white rounded-lg  ">
            {session.isLogged && <>
                <ReplyForm
                    placeholder="Share your thoughts"
                    action={{
                        type: "commentCommentable",
                        commentableId: props.commentableId,
                        onSend: onSendComment
                    }}
                    onClose={() => {}}
                />
            </>}
            {/* Root thread */}
            <div className="space-y-6 mt-8">
                {scroller.data.map((comment) => (
                    <RecursiveCommentContainer
                        key={comment.id}
                        comment={comment}
                        depth={0}
                        onDelete={adminDeleteCommentAsCommentable}
                    />
                ))}
            </div>

            {/* Loading state */}
            {loading && <Loader />}

            {/* Load more button */}
            {!loading && scroller.canFetchMore() && (
                <button
                    onClick={getMoreComments}
                    className="mt-4 w-full py-2 text-sm text-gray-500
                    hover:cursor-pointer
                    hover:text-gray-700 hover:bg-gray-50 rounded-md transition-colors"
                >
                    Load more comments
                </button>
            )}
        </div>
    </div>
    );

}
