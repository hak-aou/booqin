import {useEffect, useState} from "react";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {BookAvailability} from "../../model/book.ts";
import {useNavigate} from "react-router-dom";
import {ROUTES} from "../../routes/routes.ts";


export interface BorrowLendComponentProps {
    bookId: string
}

export default function BorrowLendComponent(props: BorrowLendComponentProps)  {
    const navigate = useNavigate();
    const sessionMethods = useSessionMethods();
    const [availability, setAvailability] = useState<BookAvailability>({
        supply: 0, demand: 0, isBorrowed: true,
        isLent: true, bookId: props.bookId, isInCart: false, inTx: null
    });

    useEffect(() => {
        fetchBookAvailability();
        const intervalId = setInterval(() => {
            fetchBookAvailability();
        }, 10_000);
        return () => clearInterval(intervalId);
    }, [props.bookId, sessionMethods.session.isLogged]);

    const fetchBookAvailability = () => {
        if(sessionMethods.session.isLogged) {
            sessionMethods.api.getBookAvailability(props.bookId).then((response: BookAvailability) => {
                setAvailability(response);
                console.log(response);
            });
        }
    };

    if(!sessionMethods.session.isLogged) {
        return <></>
    }

    return (
        <div
            className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4">

            {!availability.isLent && !availability.isInCart && !availability.inTx &&
                <button
                    className={`text-white p-2 rounded-lg  
                                                ${availability.isBorrowed ? 'bg-gray-400 hover' : 'bg-orange-400  hover:bg-orange-500 bg-orange-400 hover:cursor-pointer'}
                                                    font-semibold mr-4`}
                    onClick={() => {
                        if(availability.isBorrowed) {
                            return
                        }
                        sessionMethods.api.borrowBook(props.bookId).then(() => {
                            fetchBookAvailability();
                        });
                    }}>
                    {availability.isBorrowed ? 'In the waitlist' : availability.supply > 0 ? 'Borrow' : 'Join the waitlist'}
                </button>
            }
            {availability.isBorrowed || (availability.inTx) && <>
                { availability.inTx.orderId &&
                    <div className="text-primary font-bold text-center hover:cursor-pointer hover:underline hover:text-blue-500"
                         onClick={() => navigate(ROUTES.transaction.url.replace(":orderId", availability.inTx?.orderId!).replace(":txId", availability.inTx!.txId))}>
                        Someone is lending this book to you
                    </div>
                }
                {!availability.inTx &&
                    <button
                        className="text-white p-2 rounded-lg hover:cursor-pointer bg-primary hover:bg-mint-500
                                                    font-semibold"
                        onClick={() => {
                            sessionMethods.api.unBorrowBook(props.bookId).then(() => {
                                fetchBookAvailability();
                                console.debug("Unborrow request sent");
                            });
                        }}>
                        Leave the waitlist
                    </button>
                }
            </>}
            { !(availability.isBorrowed || availability.isLent) && !availability.isInCart && !availability.inTx &&
                <button
                    className="text-white p-2 rounded-lg hover:cursor-pointer bg-primary hover:bg-mint-500
                                                    font-semibold"
                    onClick={() => {
                        sessionMethods.api.lendBook({bookId : props.bookId, quantity: 1}).then(() => {
                            fetchBookAvailability();
                            console.debug("Lend request sent");
                        });
                    }}>Lend</button>
            }
            {(availability.isLent || (availability.inTx && availability.inTx.orderId === null)) && <>
                { availability.inTx &&
                    <div className="text-primary font-bold text-center hover:cursor-pointer hover:underline hover:text-blue-500"
                         onClick={() => navigate(ROUTES.lendTransaction.url.replace(":txId", availability.inTx!.txId))}>
                        You are lending this book to someone
                    </div>
                }
                {!availability.inTx &&
                    <div className="text-primary font-bold text-center">
                        You are lending this book&nbsp;
                    </div>
                }
            </>}
            {
                availability.isInCart &&
                <div className="text-primary font-bold text-center">
                    This book is in your cart
                </div>
            }
        </div>
    );
}