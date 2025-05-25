import {useNavigate} from "react-router-dom";
import {ROUTES} from "../../../routes/routes.ts";

type LendNavView = "To Lend" | "Loans"

export interface LendNavProps {
    view: LendNavView;
}

export default function LendNav({view}: LendNavProps) {
    const navigate = useNavigate();

    return <>
        <div className="flex justify-center">
            <div className="w-3/4 p-2 bg-white  h-full border-l-2 border-r-2 border-b-2 border-gray-100 ">
                <div className="flex items-center justify-center">
                    <button className={`p-2 hover:cursor-pointer
                            ${view === "To Lend" ? "bg-blue-500 text-white" : "hover:bg-gray-200 text-gray-500"}`}
                            onClick={() => navigate(ROUTES.toLend.url)}
                    >
                        To Lend
                    </button>
                    <button className={`p-2 hover:cursor-pointer 
                    ${view === "Loans" ? "bg-blue-500 text-white" : "hover:bg-gray-200 text-gray-500"}`}
                            onClick={() => navigate(ROUTES.lendTransactions.url)}
                    >
                        Transactions
                    </button>
                </div>
            </div>
        </div>
    </>
}
