import {useNavigate} from "react-router-dom";
import {useEffect, useState} from "react";
import {LoanOffer} from "../../../model/book.ts";
import {useSessionMethods} from "../../../hooks/session/sessionContext.tsx";
import LendNav from "./LendNav.tsx";
import {IoIosRemoveCircleOutline} from "react-icons/io";

export default function Lend() {
    const navigate = useNavigate();
    const sessionMethods = useSessionMethods();
    const [offers, setOffers] = useState<LoanOffer[]>([]);

    useEffect(() => {
        fetchWaitLists();
        const intervalId = setInterval(() => {
            fetchWaitLists();
        }, 10_000);
        return () => clearInterval(intervalId);
    }, []);

    const fetchWaitLists = () => {
        sessionMethods.api.getLoanOffers().then((response: LoanOffer[]) => {
            setOffers(response);
            console.log(response);
        });
    };

    return <>
        <LendNav view={"To Lend"}/>
        <div className="flex justify-center min-h-screen">
            <div className="w-3/4 p-4 bg-white rounded-lg shadow-lg h-full">
                <div className="p-4">
                    {offers.length === 0 &&
                        <div className="text-center text-gray-500">
                            <p>The books you lend will appear here</p>
                        </div>
                    }
                    {offers.map((offer) => {
                        return <>
                            <div className="bg-white shadow-md rounded p-2 mb-4 hover:cursor-pointer hover:bg-gray-100"
                                 onClick={() => navigate(`/book/${offer.bookId}`)}
                            >
                                <div className="flex items-center justify-between">
                                    <div className="flex items-center gap-4">
                                        <img src={offer.images.small} alt="avatar" className="rounded-full w-10 h-10"/>
                                        <h2 className="text-xl font-semibold">{offer.title}</h2>
                                    </div>
                                    {/*<div className="flex items-center gap-3">
                                        {offer.quantity} available
                                    </div>*/}
                                    <div className="flex items-center gap-8 text-xs p-2">
                                        <button
                                            className="text-red-500 px-4 py-2 rounded-lg hover:text-red-600 hover:cursor-pointer p-3 hover:bg-gray-200"
                                            onClick={(event) => {
                                                event.stopPropagation();
                                                sessionMethods.api.unLendBook(offer.bookId).then(fetchWaitLists)
                                            }}
                                        >
                                            <IoIosRemoveCircleOutline
                                                className="text-2xl"
                                            />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </>})
                    }
                </div>

            </div>
        </div>
    </>
}