import {ROUTES} from "../../../routes/routes.ts";
import {useNavigate} from "react-router-dom";

type BorrowNavView = "waitlist" | "transactions" | "other";

export interface BorrowNavProps {
    view: BorrowNavView;
}

export default function BorrowNav({view}: BorrowNavProps) {
    const navigate = useNavigate();

    return <>
        <div className="flex justify-center ">
            <div className="w-3/4 p-2 bg-white  h-full border-l-2 border-r-2 border-b-2 border-gray-100 ">
                <div className="flex items-center justify-center">
                    <button className={`p-2 hover:cursor-pointer
                            ${view === "waitlist" ? "bg-blue-500 text-white" : "hover:bg-gray-200 text-gray-500"}`}
                            onClick={() => navigate(ROUTES.waitlists.url)}
                    >
                        Waitlist
                    </button>
                    <button className={`p-2 hover:cursor-pointer 
                    ${view === "transactions" ? "bg-blue-500 text-white" : "hover:bg-gray-200 text-gray-500"}`}
                            onClick={() => navigate(ROUTES.transactions.url)}
                    >
                        Transactions
                    </button>
                </div>
            </div>
        </div>
    </>
}



