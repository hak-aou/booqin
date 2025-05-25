


export default function Loader() {
    return <>
        <div className="flex justify-center py-4">
            <div className="animate-pulse flex space-x-2">
                <div
                    className="animate-spin inline-block size-6 border-[3px] border-current border-t-transparent text-green-600 rounded-full"
                    role="status" aria-label="loading">
                    <span className="sr-only">Loading...</span>
                </div>
            </div>
        </div>
    </>
}