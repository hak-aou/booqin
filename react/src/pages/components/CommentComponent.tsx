function ShowNumberCommentComponent({ numberComment = 0 }: { numberComment: number }) {
    return (
        <div className="inline-flex items-center px-1 py-1 text-sm font-medium text-center border rounded-full bg-gray-100 ">

            <div className="flex items-center space-x-2 justify-center w-15">
                {/* Comment */}
                <button className="p-2 rounded-full bg-white hover:bg-gray-300 active:bg-gray-500">
                    <svg fill="currentColor" height="16" icon-name="downvote-outline" viewBox="0 0 20 20" width="16" xmlns="http://www.w3.org/2000/svg">
                        <path d="M10 19H1.871a.886.886 0 0 1-.798-.52.886.886 0 0 1 .158-.941L3.1 15.771A9 9 0 1 1 10 19Zm-6.549-1.5H10a7.5 7.5 0 1 0-5.323-2.219l.54.545L3.451 17.5Z"></path>
                    </svg>
                </button>

                {/* Number comments */}
                <span className="text-2xl font-bold"> {numberComment} </span>
            </div>

        </div>

    );
}



export default ShowNumberCommentComponent;
