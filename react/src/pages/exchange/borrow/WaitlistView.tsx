import {useNavigate} from "react-router-dom";
import {useSessionMethods} from "../../../hooks/session/sessionContext.tsx";
import {useEffect, useState} from "react";
import {WaitListStatus} from "../../../model/book.ts";
import {formatDateWithTime} from "../../../utils/date.ts";
import {BsHourglassSplit} from "react-icons/bs";
import {IoIosRemoveCircleOutline} from "react-icons/io";
import BorrowNav from "./BorrowNav.tsx";

export function WaitlistView() {
    const navigate = useNavigate();
    const sessionMethods = useSessionMethods();
    const [waitlists, setWaitlists] = useState<WaitListStatus[]>([]);

    useEffect(() => {
        fetchWaitLists();
        const intervalId = setInterval(() => {
            fetchWaitLists();
        }, 8_000);
        return () => clearInterval(intervalId);
    }, []);

    const fetchWaitLists = () => {
        sessionMethods.api.getWaitLists().then((response: WaitListStatus[]) => {
            setWaitlists(response);
            console.log(response);
        });
    };

    return <>
        <BorrowNav view={"waitlist"}/>
        <div className="flex justify-center min-h-screen ">
            <div className="w-3/4 p-4 bg-gray rounded-lg shadow-lg h-full">
                <div className="p-4">
                    {waitlists.length === 0 &&
                        <div className="text-center text-gray-500">
                            <p>You are not currently on any waitlist</p>
                        </div>
                    }
                    <div className="mb-10 bg-white ">
                        {waitlists.map((waitlist, index) => {
                            return <>
                                <div className="  " key={"waitbook-" + index}>
                                    <div className="bg-white shadow-md rounded  mb-4 hover:cursor-pointer "
                                         onClick={() => navigate(`/book/${waitlist.bookId}`)}
                                         key={waitlist.bookId}
                                    >
                                        <div className="flex hover:bg-gray-50 items-center justify-between">
                                            <div className="flex items-center gap-4 p-2">
                                                <img src={waitlist.images.small} alt="avatar" className="rounded-full w-10 h-10"/>
                                                <h2 className="text-xl font-semibold">{waitlist.title}</h2>
                                            </div>
                                            {waitlist.isLocked &&
                                                <div className="text-xs p-2">
                                                    <p>
                                                        Now available. Locked for you until
                                                        <span
                                                            className="text-primary font-bold"> {formatDateWithTime(waitlist.lockedUntil)}
                                                            <BsHourglassSplit
                                                                className={`text-orange-500 text-xl `}
                                                            />
                                                </span>
                                                    </p>
                                                    <button
                                                        className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 hover:cursor-pointer"
                                                        onClick={(event) => {
                                                            event.stopPropagation();
                                                            sessionMethods.api.waitlistAccept(waitlist.bookId).then(fetchWaitLists)
                                                        }}
                                                    >
                                                        Accept
                                                    </button>
                                                </div>
                                            }
                                            <div className="flex items-center gap-8 text-xs p-2">
                                                <button
                                                    className="text-red-500 px-4 py-2 rounded-lg hover:text-red-600 hover:cursor-pointer p-3 hover:bg-gray-200"
                                                    onClick={(event) => {
                                                        event.stopPropagation();
                                                        sessionMethods.api.unBorrowBook(waitlist.bookId).then(fetchWaitLists)
                                                    }}
                                                >
                                                    <IoIosRemoveCircleOutline
                                                        className="text-2xl"
                                                    />
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </>
                        })}
                    </div>

                </div>
            </div>
        </div>
    </>
}