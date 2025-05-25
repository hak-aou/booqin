import {BookInfo} from "../../model/book.ts";
import {useNavigate} from "react-router-dom";
import {ROUTES} from "../../routes/routes.ts";
import AddToCollectionButton from "../../pages/collections/AddToCollectionButton.tsx";
import {MdDelete} from "react-icons/md";
import coverNotAvailable from '../../../../src/main/resources/images/cover_not_available.jpg';
import {useSession} from "../../hooks/session/sessionContext.tsx";

interface BookPreviewComponentProps {
    book: BookInfo,
    iAmOwner?: boolean,
    removeBook?: (book: BookInfo) => void
}

function BookPreviewComponent({book, removeBook, ...props}: BookPreviewComponentProps) {
    const navigate = useNavigate();
    const session = useSession();

    return (
        <div key={book.id} className="
                                    bg-white p-2 rounded-lg
                                    mb-2
                                    transition-shadow duration-300 hover:cursor-pointer"
             onClick={() => navigate(ROUTES.books.url.replace(":bookId", book.id))}
        >
            <div className="flex gap-4 items-center">
                {/* Book info - left side */}
                <img src={book.imageLinks ? book.imageLinks.medium : coverNotAvailable}  alt="book cover" className="w-16 object-cover rounded" loading="lazy"/>
                <div className="flex flex-col">
                    <h2 className="text-md font-semibold">{book.title}</h2>
                    <p className="text-gray-600 text-sm">{book.subtitle}</p>
                </div>

                {/* Buttons container - right side */}
                <div className="flex gap-2 ml-auto"
                     onClick={(event) => {
                         event.stopPropagation();
                     }}
                >
                    {/* Main buttons column */}
                    <div className="flex flex-col gap-2">
                        {/*<button className="text-gray-500 p-2 rounded-lg hover:cursor-pointer hover:bg-gray-100 hover:text-teal-700 font-semibold">
                                            </button>*/}
                        {session.isLogged &&
                            <AddToCollectionButton bookId={book.id} bookTitle={book.title}/>
                        }
                        {props.iAmOwner && (
                            <button
                                className="text-gray-400 p-2 rounded-lg hover:cursor-pointer hover:text-red-500 hover:bg-red-100 font-semibold text-center flex justify-center items-center"
                                onClick={(event) => {
                                    event.stopPropagation();
                                    removeBook && removeBook(book);
                                }}
                            >
                                <MdDelete/>
                            </button>
                        )}
                    </div>

                    {/* More actions button
                    <button
                        className="text-gray-500 p-2 rounded-lg hover:cursor-pointer hover:bg-gray-100 font-semibold"
                        onClick={(event) => {
                            event.stopPropagation();
                            if (selectedBook?.id === book.id) {
                                setSelectedBook(undefined);
                                return;
                            }
                            setSelectedBook(book);
                        }}
                    >
                        ⌵
                    </button>*/}
                </div>
            </div>
            {/*<div className="flex gap-4">
                <img src={book.imageLinks ? book.imageLinks.medium : ''} alt="book cover" className="w-16 object-cover rounded-t-lg"/>
                <div className="flex flex-col">
                    <h2 className="text-md font-semibold">{book.title}</h2>
                    <p className="text-gray-600 text-sm">{book.subtitle}</p>
                </div>
                <div className="flex flex-col ml-auto"
                     onClick={(event) => {
                         event.stopPropagation();
                         console.warn("todo: add to collection");
                     }}
                >
                    <button
                        className=" text-gray-500 p-2 rounded-lg hover:cursor-pointer font-semibold"
                    >
                        <CiBookmarkPlus className="text-2xl"/>
                    </button>
                </div>
            </div>*/}
        </div>
    )
}

export default BookPreviewComponent;