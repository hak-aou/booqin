import {Comment, CommentData} from "../../model/comment.ts";
import {DataPageScroller, PageRequest} from "../../model/common.ts";
import {useState} from "react";
import {useNavigate} from "react-router-dom";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {formatDateWithHint} from "../../utils/date.ts";
import {FiMessageSquare} from "react-icons/fi";
import ReplyForm from "./ReplyForm.tsx";
import {CiCircleMinus, CiCirclePlus} from "react-icons/ci";
import {MdDeleteOutline, MdModeEditOutline, MdMoreHoriz} from "react-icons/md";
import Loader from "../Loader.tsx";
import ConfirmationModal from "../ConfirmationModal.tsx";
import {RiDeleteBin2Line} from "react-icons/ri";
import {SmallVoteButtons} from "../vote/VoteButtons.tsx";
import VoteComponent from "../vote/VoteComponent.tsx";

interface RecursiveCommentContainerProps {
    comment: Comment;
    depth: number;
    hoveredCallback?: (hovered: boolean) => void;
    stopHoveredCallback?: (hovered: boolean) => void;
    onDelete: (comment: Comment) => void;
}

export default function RecursiveCommentContainer({ depth, ...props }: RecursiveCommentContainerProps) {

    const [currentComment, setCurrentComment] = useState(props.comment);
    const fetcher = (page: PageRequest) =>
        sessionMethods.api.getReplies(currentComment.id, page)
    const [scroller, setScroller] = useState<DataPageScroller<Comment>>(
        new DataPageScroller<Comment>(currentComment.replies, currentComment.repliesCount, 0, 10, fetcher)
    );
    const [isExpanded, setIsExpanded] = useState(true);
    const navigate = useNavigate();
    const [replyFormOpen, setReplyFormOpen] = useState(false);
    const sessionMethods = useSessionMethods();
    const [loading, setLoading] = useState(false);
    const hasReplies = !scroller.empty();
    const [isHovered, setIsHovered] = useState(false);
    const [isEditingCurrentComment, setIsEditingCurrentComment] = useState(false);

    const [deleteModalOpen, setDeleteModalOpen] = useState(false);
    const [modal, setModal] = useState<Element | null>(null);
    const deleteModal = (message: string, onConfirm: () => void) => <ConfirmationModal
            title="Delete comment"
            message={message}
            onConfirm={onConfirm}
            onCancel={() => setDeleteModalOpen(false)}
    />

    async function getMoreReplies() {
        setLoading(true);
        try {
            scroller.fetchMoreData().then(newScroller => {
                    setScroller(newScroller)
                }
            );
        } finally {
            setLoading(false);
        }
    }

    function addFromReplyForm(commentData: CommentData) {
        const comment = {
            ...commentData,
            replies: [],

        } as Comment;
        const newScroller = new DataPageScroller<Comment>(
            [comment, ...scroller.data],
            scroller.numberResultMax + 1,
            scroller.offset + 1,
            scroller.limit,
            fetcher
        );
        setScroller(newScroller);
        setReplyFormOpen(false);
    }

    function editFromReplyForm(commentData: CommentData) {
        console.log(commentData);
        setCurrentComment({
            ...currentComment,
            content: commentData.content
        })
        setIsEditingCurrentComment(false);
    }

    function removeComment() {
        sessionMethods.api.obfuscateComment(currentComment.id)
            .then(() => {
                setCurrentComment({
                    ...currentComment,
                    content: "",
                    author: null,
                });
                setDeleteModalOpen(false)
            });
        if(scroller.data.length > 0) {
            return;
        }
        props.onDelete(currentComment);
    }

    function adminRecursiveDeleteComment_AsReply() {
        sessionMethods.api.deleteComment(currentComment.id)
            .then(() => {
                props.onDelete(currentComment);
            });
    }

    function adminDeleteCommentAsParentComment(comment: Comment) {
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
            fetcher
        ));
    }


    function hover() {
        setIsHovered(true);
        props.hoveredCallback && props.hoveredCallback(true);
    }

    function stopHover() {
        props.stopHoveredCallback && props.stopHoveredCallback(false);
        setIsHovered(false);
    }

    const adminDeleteButton = <>
        <button
            onClick={() => {
                // @ts-ignore
                setModal(deleteModal("Are you sure you want to delete this comment? This will also delete all replies.",
                    adminRecursiveDeleteComment_AsReply))
                setDeleteModalOpen(true)

            }}
            className={`flex items-center space-x-1 hover:text-gray-700 cursor-pointer 
                      ${sessionMethods.loggedAndAdmin && !(currentComment.author?.id === sessionMethods.user?.id) 
                        ? 'text-orange-500' : ''}`
                        }
        >
            <RiDeleteBin2Line />
            <span>
                Delete {scroller.numberResultMax > 0 ? 'all' : ''}
            </span>
        </button>
    </>

    return (
        <div className={`relative `} onMouseEnter={hover} onMouseLeave={stopHover}>
            {/* Comment header */}
            {hasReplies && depth === 0 && <>
                <div
                    className={`absolute z-1 left-3 top-0 bottom-0 
                        w-[1px] bg-gray-200 ${isHovered ? 'bg-primary transition-colors': ''} `} />
            </>}
            <div className="flex items-center space-x-2 ">
                {depth > 0 && (
                    <div
                        className={`z-1 mr-[0px] left-2 top-0 bottom-0
                        w-[14px] h-[1px] bg-gray-200 ${isHovered ? 'bg-primary transition-colors': ''} `} />
                )}
                {currentComment.author !== null && <>
                    <img
                        src={currentComment.author.imageUrl}
                        alt={currentComment.author.username}
                        className="w-6 h-6 rounded-full z-2 cursor-pointer"
                        onClick={() => navigate(`/profile/${currentComment.author?.id}`)}
                    />
                    <span className="text-sm font-medium text-gray-900 on-hover:underline cursor-pointer"
                          onClick={() => navigate(`/profile/${currentComment.author?.id}`)}>
                        {currentComment.author.username}
                    </span>
                    <span className="text-xs text-gray-500">
                            · {formatDateWithHint(currentComment.createdAt)}
                    </span>
                </>}
                {currentComment.author === null && <>
                    <div className="w-6 h-6 rounded-full z-2 bg-gray-200 cursor-pointer"/>
                    <span className="text-sm font-medium text-gray-900">
                        comment deleted
                    </span>
                </>}

            </div>
            <div className="flex">
                <div className={`w-${depth === 0 ? '[13px]' : '[25px]'} 
                                ${depth > 0 ? `pl-5 border-r-1 border-gray-100` : ''}
                                ${isHovered ? 'border-gray-400' : ''}
                                `}>
                    &nbsp;
                </div>
                <div className="flex-1 p-0">
                    {(currentComment.author || sessionMethods.loggedAndAdmin) && <>
                        <div className={` ${depth > 0 ? 'border-l-2 border-gray-300' : ''}  ${isHovered ? 'border-primary' : ''} `}>
                            {!isEditingCurrentComment &&
                                <div className="mb-4 text-sm text-gray-700 pl-5 pt-2">
                                    {currentComment.content}
                                </div>
                            }
                            {isEditingCurrentComment && sessionMethods.isLogged && <>
                                <ReplyForm
                                    placeholder="Edit your comment"
                                    action={{
                                        type: "edit",
                                        comment: currentComment,
                                        onEdit: editFromReplyForm
                                    }}
                                    onClose={() => setIsEditingCurrentComment(false)}
                                />
                            </>}
                            {/* Comment actions */}
                            <div className="flex items-center space-x-4 text-xs text-gray-500 pl-4 ">

                                <VoteComponent
                                    sessionMethods={sessionMethods}
                                    votableId={currentComment.votableId}
                                    ButtonComponent={SmallVoteButtons}
                                />

                                {sessionMethods.isLogged && <>
                                    <button
                                        className="flex items-center space-x-1 hover:text-gray-700 cursor-pointer"
                                        onClick={() => setReplyFormOpen(!replyFormOpen)}
                                    >
                                        <FiMessageSquare className="w-4 h-4"/>
                                        <span>Reply</span>
                                    </button>
                                </>}
                                {hasReplies && (
                                    <button
                                        className="flex items-center space-x-1 ">
                                        <span>{currentComment.replies.length} repl{currentComment.replies.length > 1 ? 'ies' : 'y'}</span>
                                    </button>
                                )}
                                {currentComment.author?.id === sessionMethods.user?.id && !isEditingCurrentComment && <>
                                    <button
                                        onClick={() => setIsEditingCurrentComment(true)}
                                        className="flex items-center space-x-1 hover:text-gray-700 cursor-pointer">
                                        <MdModeEditOutline/>
                                        <span>Edit</span>
                                    </button>
                                </>}
                                {(currentComment.author?.id === sessionMethods.user?.id || sessionMethods.loggedAndAdmin)
                                    && currentComment.author
                                    && !isEditingCurrentComment && <>
                                        <button
                                            onClick={() => {
                                                // @ts-ignore
                                                setModal(deleteModal("Are you sure you want to delete this comment?",
                                                    removeComment))
                                                setDeleteModalOpen(true)

                                            }}
                                            className={`flex items-center space-x-1 hover:text-gray-700 cursor-pointer 
                                        ${sessionMethods.loggedAndAdmin && !(currentComment.author?.id === sessionMethods.user?.id) ? 'text-orange-500' : ''}`}>
                                            <MdDeleteOutline/>
                                            <span>{sessionMethods.loggedAndAdmin && currentComment.author?.id !== sessionMethods.user?.id ? 'Moderate' : 'Remove'}</span>
                                        </button>
                                    </>}
                                {sessionMethods.loggedAndAdmin && <> {adminDeleteButton} </>}
                            </div>
                        </div>
                    </>}
                    {!currentComment.author && <>
                     <div className={`border-l-2 border-gray-300 ${isHovered ? 'border-primary' : ''} min-h-[1em]`}>

                     </div>
                    </>}
                    {/* New comment form */}
                    {replyFormOpen && <>
                        <ReplyForm
                            placeholder="Reply to this comment"
                            action={{
                                type: "reply",
                                comment: props.comment,
                                onSend: addFromReplyForm
                            }}
                            onClose={() => setReplyFormOpen(false)}
                        />
                    </>}
                    {scroller.canFetchMore() && <>
                        <MdMoreHoriz
                            onClick={getMoreReplies}
                            className="w-5 h-5 text-gray-300 cursor-pointer bg-gray-100
                                rounded-full z-1  hover:bg-gray-200 ml-[-9px]"
                        />
                    </>}
                    <div className="flex ml-[-10px]">
                        {scroller.data.length > 0 && <>
                            {isExpanded &&
                                <CiCircleMinus
                                    onClick={() => setIsExpanded(!isExpanded)}
                                    className="w-5 h-5 text-gray-300 cursor-pointer bg-gray-100
                                    rounded-full z-1  hover:bg-gray-200"
                                />
                            }
                            {!isExpanded &&
                                <CiCirclePlus
                                    onClick={() => setIsExpanded(!isExpanded)}
                                    className="w-5 h-5 text-gray-300 cursor-pointer bg-gray-100
                                    rounded-full z-1  hover:bg-gray-200"
                                />

                            }
                        </>
                        }
                    </div>
                    {/* Loading state */}
                    {loading && <Loader/>}
                    {/* Nested replies */}
                    {hasReplies && (
                        <div className={`mt-3 space-y-4 ${isExpanded ? '' : 'hidden'}`}>
                            {scroller.data.map(reply => (
                                <RecursiveCommentContainer
                                    key={reply.id}
                                    comment={reply}
                                    depth={depth + 1}
                                    hoveredCallback={hover}
                                    stopHoveredCallback={stopHover}
                                    onDelete={adminDeleteCommentAsParentComment}
                                />
                            ))}
                        </div>
                    )}
                </div>
            </div>
            {deleteModalOpen && <> {modal}
            </>}
        </div>
    );
}