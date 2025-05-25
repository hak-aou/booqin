import {loanWorkflowResolverBorrow} from "../../../model/Cart.ts";
import {useNavigate} from "react-router-dom";
import {GiBlackBook} from "react-icons/gi";
import {ROUTES} from "../../../routes/routes.ts";
import {RiBillFill} from "react-icons/ri";
import {useOrders} from "../../../hooks/borrowProvider.tsx";
import BorrowNav from "./BorrowNav.tsx";
import {formatDateWithTime} from "../../../utils/date.ts";
import {StepIcon} from "../../cart/OrderDetails.tsx";

export function TransactionsView() {
    const navigate = useNavigate();
    const orders = useOrders();

    return <>
        <BorrowNav view={"transactions"}/>
        <div className="flex justify-center min-h-screen">
            <div className="w-3/4 p-4 bg-gray rounded-lg shadow-lg h-full">
                <div className="p-4">
                    {orders.length === 0 &&
                        <div className="text-center text-gray-500">
                            <p>
                               Your borrow transactions will appear here
                            </p>
                        </div>
                    }
                    <div className="mb-10 ">
                        {
                            orders
                                .flatMap(order => order.bookTransactions)
                                .sort((a,b) => {
                                    const oldestStepA = a.steps.sort((a, b) => a.date.localeCompare(b.date))[0];
                                    const oldestStepB = b.steps.sort((a, b) => a.date.localeCompare(b.date))[0];
                                    return -oldestStepA.date.localeCompare(oldestStepB.date);
                                })
                                .map((tx, index) => {
                                    const totalBooks = tx.books.length;
                                    const oldestStep = tx.steps.sort((a, b) => a.date.localeCompare(b.date))[0];
                                    return <>
                                        <div key={index} className={`flex flex-wrap items-center gap-4 p-2 
                                        ${index % 2 === 0 ? 'bg-gray-50' : 'bg-white'}
                                        hover:cursor-pointer hover:bg-gray-200 mt-4 `}
                                             onClick={() => navigate(ROUTES.transaction.url.replace(":orderId", orders[index].orderId).replace(":txId", tx.txId))}>
                                            <div>
                                                <img src={tx.ownerProfile.avatar} alt="avatar" className="rounded-full w-10 h-10"/>
                                            </div>
                                            <div>
                                                <div className="font-medium">{tx.ownerProfile.username}</div>
                                            </div>
                                            <div>
                                                <p className={`inline-block px-2 py-1 text-sm rounded-md bg-green-100 text-green-800`}>
                                                    <GiBlackBook className="inline-block"/>&nbsp;{totalBooks}
                                                </p>
                                            </div>
                                            <div className="inline-block px-2 py-1 text-sm rounded-md bg-gray-50 text-green-800 "
                                            >
                                                {(() => {
                                                    const lastStep = tx.steps.sort((a, b) => a.date.localeCompare(b.date)).reverse()[0];
                                                    return <>
                                                        {(() => {
                                                            const workflow = loanWorkflowResolverBorrow(lastStep.type);
                                                            return <div className={`flex items-center gap-2 text-sm ${workflow.nextStep ? 'border-orange-400 border-2 p-1 rounded-md' : ''}`}>
                                                                {workflow.nextStep && <>
                                                                    <StepIcon type={workflow.nextStep.type}/>
                                                                    <span>{workflow.nextStep.meaning}</span>
                                                                </>}
                                                                {!workflow.nextStep && <>
                                                                    <StepIcon type={lastStep.type}/>
                                                                    <span className="text-gray-500">{workflow.interpret}</span>
                                                                </>}
                                                            </div>;
                                                        })()}
                                                    </>;
                                                })()}
                                            </div>
                                            <p className="inline-block px-2 py-1 text-sm rounded-md bg-gray-100 text-green-800 hover:cursor-pointer hover:bg-gray-200"
                                               onClick={() => navigate(ROUTES.order.url.replace(":orderId", orders[index].orderId))}
                                            >
                                                <RiBillFill className="inline-block"/>&nbsp;see order
                                            </p>
                                            <span className="inline-block px-2 py-1 text-sm">{formatDateWithTime(oldestStep.date)}</span>
                                        </div>
                                    </>
                                })
                        }
                    </div>

                </div>

            </div>
        </div>
    </>
}