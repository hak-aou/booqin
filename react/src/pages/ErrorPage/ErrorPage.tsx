function ErrorPage({
                          title = "Not found",
                          errorDescription = "The content you are trying to access does not exist"
                     }) {
    return <>
        <main className="max-w-7xl mx-auto">
            <div className="flex justify-center min-h-screen bg-gray-100">
                <div className="w-3/4 p-4 bg-white rounded-lg shadow-lg h-full">
                    <div className="p-4">
                        <h1 className="font-bold">{title}</h1>
                        <p>{errorDescription}</p>
                    </div>
                </div>
            </div>
        </main>
    </>
}

export default ErrorPage