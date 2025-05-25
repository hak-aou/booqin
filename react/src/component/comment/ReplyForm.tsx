import {Comment, CommentData} from "../../model/comment.ts";
import {useEffect, useState} from "react";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";

type ReplyFormAction =
    | {type: "reply", comment: Comment, onSend: (comment: CommentData) => void}
    | {type: "edit", comment: Comment, onEdit: (comment: CommentData) => void}
    | {type: "commentCommentable", commentableId: string, onSend: (comment: CommentData) => void}

interface ReplyFormProps {
    action : ReplyFormAction;
    onClose: () => void;
    placeholder?: string;

}
export default function ReplyForm({ action, ...props }: ReplyFormProps) {
    const [reply, setReply] = useState('');
    const session = useSessionMethods();
    const [, setSent] = useState(false);
    const [dirty, setDirty] = useState(false);

    useEffect(() => {
        if(action.type === "edit" && !dirty) {
            setReply(action.comment.content);
        }
    });

    function sendReply() {
        if (reply === '' || !reply.trim()) {
            return;
        }
        switch (action.type) {
            case "edit":
                if(reply === action.comment.content) {
                    setSent(true);
                    props.onClose();
                    return;
                }
                const editedComment = {
                    ...action.comment,
                    content: reply
                };
                session.api.editComment(editedComment).then(() => {
                    setReply('');
                    setSent(true);
                    action.onEdit(editedComment);
                    props.onClose();
                });
                break;
            case "reply":
                session.api.replyToComment(action.comment.id, reply)
                    .then((comment: CommentData) => {
                        setReply('');
                        setSent(true);
                        props.onClose();
                        action.onSend(comment);

                    });
                break;
            case "commentCommentable":
                console.debug("comment", " commentableId", action.commentableId);
                session.api.commentCommentable(action.commentableId, reply)
                    .then((comment: CommentData) => {
                        setReply('');
                        setSent(true);
                        console.debug("comment", comment, " commentableId", action.commentableId);
                        action.onSend(comment);
                    });
                break;
        }
    }

    function updateReply(text: string) {
        setReply(text);
        setDirty(true);
    }

    function cancel() {
        if(action.type == "commentCommentable") {
            setReply('');
        } else {
            props.onClose();
        }
    }

    return <div className=" mt-5">
        <div className="flex">
            <div className="flex-1 p-0">
                <div className="mb-4 text-sm text-gray-700 pl-4 pt-2">
                    <textarea className="w-full h-20 border-2 border-gray-200 rounded-lg p-2"
                              placeholder={props.placeholder || ""}
                              value={reply}
                              onChange={(e) => updateReply(e.target.value)}
                    />
                </div>
                {/* Comment actions cancel / comment */}
                <div className="flex flex-row-reverse items-center
                gap-3
                space-x-4 text-xs text-gray-500 pl-4">

                    <button className="flex items-center space-x-1
                    hover:text-gray-700
                    bg-primary text-white px-2 py-1 rounded-lg
                    cursor-pointer"
                            onClick={sendReply}
                    >
                        <span>Comment</span>
                    </button>
                    <button className="flex items-center space-x-1
                     bg-gray-200 text-gray-700 px-2 py-1 rounded-lg
                     hover:text-gray-700 cursor-pointer"
                            onClick={cancel}
                    >
                        <span>Cancel</span>
                    </button>
                </div>
            </div>
        </div>
    </div>;
}