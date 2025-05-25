import Book from "../../model/book.ts";
import {useEffect, useState} from "react";
import {useParams} from "react-router";
import {API_ENDPOINTS} from "../../api/endpoints.ts";
import ErrorPage from "../ErrorPage/ErrorPage.tsx";
import VoteComponent from "../../component/vote/VoteComponent.tsx";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import BorrowLendComponent from "../../component/book/BorrowLendComponent.tsx";
import AddToCollectionButton from "../collections/AddToCollectionButton.tsx";
import {PageRequest} from "../../model/common.ts";
import FollowComponent from "../../component/followers/FollowComponent.tsx";
import {BigVoteButtons} from "../../component/vote/VoteButtons.tsx";
import {CommentSection} from "../../component/comment/CommentSection.tsx";
import coverNotAvailable from '../../../../src/main/resources/images/cover_not_available.jpg';
import Loader from "../../component/Loader.tsx";

async function fetchBook(bookId: string): Promise<Book> {
    const url = API_ENDPOINTS.root + API_ENDPOINTS.book.replace(':bookId', bookId);
    console.log("URL :", url);

    const response = await fetch(url);

    if (!response.ok) {
        const errorText = await response.text();
        console.error("Erreur lors de la récupération :", errorText);
        throw new Error('Erreur lors de la récupération des données du livre');
    }

    const json = await response.json();
    // console.log("Réponse JSON :", json);

    if (!json || Object.keys(json).length === 0) {
        throw new Error('Aucun livre trouvé ou JSON vide');
    }

    return {
        id: json.id ?? null,
        commentableId: json.commentableId ?? null,
        followableId: json.followableId ?? null,
        votableId: json.votableId,
        title: json.title ?? null,
        isbn: json.isbn
            ? {
                isbn_13: json.isbn.isbn_13,
                isbn_10: json.isbn.isbn_10,
            }
            : null,
        authors: json.authors ?? null,
        publishers: json.publishers ?? [],
        publishedDate: json.publishedDate ?? null,
        categories: json.categories ?? [],
        language: json.language ?? null,
        imageLinks: json.imageLinks
            ? {
                small: json.imageLinks.small,
                medium: json.imageLinks.medium,
                large: json.imageLinks.large,
            }
            : null,
        subtitle: json.subtitle ?? null,
        description: json.description ?? null,
        pageCount: json.pageCount ?? null,
    } as Book;
}


function getBookData(isbn13: string) {
    const [error, setError] = useState<string>();
    const [book, setBook] = useState<Book>();
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!isbn13) return;

        async function fetchData() {
            setLoading(true);
            // await new Promise(resolve => setTimeout(resolve, 2000));
            fetchBook(isbn13)
                .then(bookData => setBook(bookData))
                .catch(err => setError(err.message))
                .finally(() => setLoading(false));
        }

        fetchData().then();
    }, [isbn13]);

    return {book, loading, error};
}

function BookPage() {
    const {bookId} = useParams();
    if (!bookId) return <p>No isbn provided</p>;

    const {book, loading, error} = getBookData(bookId);

    const sessionMethods = useSessionMethods();

    if (error) return <ErrorPage title={"Book not found"}
                                 errorDescription={"Book with id " + bookId + " is not found"}/>;

    return (
        <>
            <main className="max-w-7xl mx-auto">
                <div className="flex justify-center min-h-screen bg-gray-100">
                    <div className="w-3/4 p-4 bg-white rounded-lg shadow-lg h-full">
                        {book && <>
                            <div className="p-4">

                                {/* Title */}
                                <h2 className="text-4xl font-extrabold">
                                    {book.title}
                                </h2>

                                <div className="flex items-center justify-between mt-4">
                                    <div></div>
                                    <div className="flex items-center gap-4">
                                        <FollowComponent followableId={book.followableId}/>
                                    </div>
                                </div>


                                { /* Image + Book information */}
                                <div className="grid grid-cols-3 gap-4 mt-4">

                                    {/* Image */}
                                    <div className="col-span-1 bg-gray-100  flex items-center justify-center rounded-lg p-4">
                                        <img
                                            src={
                                                book.imageLinks?.large
                                                || book.imageLinks?.medium
                                                || book.imageLinks?.small ||
                                                coverNotAvailable
                                            }
                                            alt="book cover"
                                            className="object-cover w-full"
                                            loading="lazy"
                                        />
                                    </div>

                                    <div className="col-span-2 bg-gray-50  p-4 rounded-lg">
                                        {/* Subtitle */}
                                        <div className="flex items-center mt-4">
                                            <h3 className="w-30 font-bold">Subtitle</h3>
                                            <h1>: {book.subtitle ?? "N/A" }</h1>
                                        </div>

                                        {/* Authors */}
                                        <div className="flex mt-4">
                                            <h3 className="w-30 font-bold">Authors</h3>
                                            <p className="w-2">: </p>
                                            {book.authors && book.authors.length > 0 ? (
                                                <div className="flex flex-wrap">
                                                    {book.authors.map((author: string, index: number) => (
                                                        <span
                                                            key={index}
                                                            className="inline-block bg-gray-200 rounded-full px-3 py-1 text-sm font-semibold text-gray-700">
                                                    {author}
                                              </span>))}
                                                </div>
                                            ) : <h1>N/A</h1>
                                            }
                                        </div>


                                        {/* ISBN */}
                                        {
                                            book.isbn == null ? (
                                                <div className="flex items-center mt-4">
                                                    <h3 className="w-30 font-bold">ISBN</h3>
                                                    <h1>: N/A</h1>
                                                </div>
                                            ) : (
                                                <>
                                                    <div className="flex items-center mt-4">
                                                        <h3 className="w-30 font-bold">ISBN 13</h3>
                                                        <h1>: {book.isbn?.isbn_13 ?? "N/A"} </h1>
                                                    </div>
                                                    <div className="flex items-center mt-4">
                                                        <h3 className="w-30 font-bold">ISBN 10</h3>
                                                        <h1>: {book.isbn?.isbn_10 ?? "N/A"} </h1>
                                                    </div>
                                                </>
                                            )
                                        }


                                        {/* Page count */}
                                        <div className="flex items-center mt-4">
                                            <h3 className="w-30 font-bold">Page count</h3>
                                            <h1>: {book.pageCount ?? "N/A"} </h1>
                                        </div>

                                        {/* Publish */}
                                        <div className="flex mt-4">
                                            <h3 className="w-30 font-bold">Publishers</h3>
                                            <p className="w-2">: </p>
                                            {book.publishers && book.publishers.length > 0 ? (
                                                <div className="flex flex-wrap">
                                                    {book.publishers.map((publisher: string, index: number) => (
                                                        <span
                                                            key={index}
                                                            className="inline-block bg-gray-200 rounded-full px-3 py-1 text-sm font-semibold text-gray-700">
                                                    {publisher}
                                              </span>))}
                                                </div>
                                            ) : <h1>&nbsp;N/A</h1>
                                            }
                                        </div>

                                        <div className="flex items-center mt-4">
                                            <h3 className="w-30 font-bold">Published date</h3>
                                            <h1>: {book.publishedDate ?? "N/A"}</h1>
                                        </div>

                                        {/* Categories */}
                                        <div className="flex mt-4">
                                            <h3 className="w-30 font-bold">Categories</h3>
                                            <p className="w-2">: </p>
                                            {book.categories && book.categories.length > 0 ? (
                                                <div className="flex flex-wrap">
                                                    {book.categories.map((category: string, index: number) => (
                                                        <span
                                                            key={index}
                                                            className="inline-block bg-gray-200 rounded-full px-3 py-1 text-sm font-semibold text-gray-700">
                                                    {category}
                                              </span>))}
                                                </div>
                                            ) : <h1>&nbsp;N/A</h1>
                                            }
                                        </div>

                                        {/* Language */}
                                        <div className="flex items-center mt-4">
                                            <h3 className="w-30 font-bold">Language</h3>
                                            <h1>: {book?.language?.toLowerCase() ?? "N/A"}</h1>
                                        </div>


                                    </div>
                                </div>

                                {/* Description */}
                                <div className="  mt-4 w-full bg-gray-50  p-4 rounded-lg">
                                    <h3 className="w-30 font-bold mb-4">Description</h3>
                                    <p>{book.description ?? "N/A"}</p>
                                </div>

                                {/* Vote + Comment + Add to collection */}
                                <div className="flex items-center w-5 mt-4 space-x-4">
                                    <VoteComponent
                                        sessionMethods={sessionMethods}
                                        votableId={book.votableId}
                                        ButtonComponent={BigVoteButtons}
                                    />
                                    <AddToCollectionButton bookId={book.id} bookTitle={book.title} />
                                </div>

                                <BorrowLendComponent bookId={book.id}></BorrowLendComponent>

                                {book?.commentableId && <CommentSection
                                    getComments={(page: PageRequest) =>
                                        sessionMethods.api.getComments(book.commentableId, page)}
                                    commentableId={book.commentableId}/>
                                }
                            </div>
                        </>}
                        {(loading || !book) && <>
                            <div className="p-4">
                                <Loader/>
                            </div>
                        </>}
                    </div>
                </div>

            </main>
        </>
    );


}

export default BookPage;