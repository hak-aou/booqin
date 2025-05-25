import RegisterComponentModal from "./RegisterComponent.tsx";
import {useState} from "react";

function AddBookToCollectionButton({isLogged, bookId}:
                                   {isLogged: boolean, bookId: string }) {
    const [modalIsOpen, setModalIsOpen] = useState<boolean>(false);

    const handleAddToCollection = () => {
        if (!isLogged) {
            console.log("Must be logged in to add to collection");
            setModalIsOpen(true);
            return;
        }
        // Add to collection
        // TODO
        console.log("Add to collection : " + bookId);
    }

    return (
        <>
            <div
                className="inline-flex items-center px-1 py-1 text-sm font-medium text-center border rounded-full bg-gray-100 ">

                <div className="flex items-center  justify-center w-38">
                    {/* Add to collection */}
                    <button className="p-2 rounded-full bg-white hover:bg-gray-300 active:bg-gray-500"
                            onClick={handleAddToCollection}
                    >
                        <svg fill="currentColor" height="16" icon-name="downvote-outline" viewBox="0 0 20 20" width="16"
                             xmlns="http://www.w3.org/2000/svg">
                            <path d="M18 4v15.06l-5.42-3.87-.58-.42-.58.42L6 19.06V4h12m1-1H5v18l7-5 7 5V3z"></path>
                        </svg>
                    </button>

                    {/* Comment */}
                    <span className="text font-bold"> Add to collection </span>
                </div>
            </div>

            {modalIsOpen && (
                <RegisterComponentModal closeModal={() => setModalIsOpen(false)}/>
            )}
        </>
    );
}


export default AddBookToCollectionButton;
