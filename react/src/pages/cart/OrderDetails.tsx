import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import {FaMoneyBill, FaCheck, FaUndo, FaHourglassHalf, FaCheckCircle} from "react-icons/fa";
import {GiBlackBook} from "react-icons/gi";
import {MdOpenInNew} from "react-icons/md";
import {FcShipped} from "react-icons/fc";
import {loanWorkflowResolverBorrow, TransactionStepType, Order} from "../../model/Cart.ts";
import {formatDate} from "../../utils/date.ts";
import {ROUTES} from "../../routes/routes.ts";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";

export const StepIcon = ({type, className}: { type: TransactionStepType, className?: string } ) => {
    switch (type) {
        case "TO_BE_SENT":
            return <FaHourglassHalf className={`${className !== undefined ? className : 'text-orange-500'}`}/>;
        case "SENT":
            return <FcShipped className={`${className !== undefined ? className : 'text-orange-500'}`}/>;
        case "RECEIVED":
            return <FaCheckCircle className={`${className !== undefined ? className : 'text-green-500'}`}/>;
        case "RETURNED":
            return <FaUndo className={`${className !== undefined ? className : 'text-purple-500'}`}/>;
        case "RECEIVED_BACK":
            return <FaCheck className={`${className !== undefined ? className : 'text-purple-500'}`}/>;
    }
};

export default function OrderDetails() {
    const navigate = useNavigate();
    const { orderId } = useParams<{ orderId: string }>();
    const sessionMethods = useSessionMethods();
    const [order, setOrder] = useState<Order | undefined>(undefined);

    useEffect(() => {
        sessionMethods.api.myBorrowTransactions()
            .then((response) => {
            const maybeOrder: Order | undefined = response
                .find((order) => order.orderId === orderId);
            setOrder(maybeOrder);
        });
    }, [orderId]);

    if (order === undefined) {
        return <>Loading...</>
    }

    return <>
        <div className="flex justify-center min-h-screen bg-gray-100">
            <div className="w-3/4 p-8 bg-white rounded-lg shadow-lg h-full">
                <div className="flex items-center gap-4">
                    <button

                        className="p-2 text-sm bg-blue-500 text-white rounded-md hover:bg-blue-600 hover:cursor-pointer"

                        onClick={() => navigate(ROUTES.cart.url)}>
                        Back to cart
                    </button>
                </div>
                <div className="flex justify-between items-center border-b pb-4 mt-4">
                    <h1 className="text-2xl font-bold">Order Details</h1>
                    <div className="flex items-center gap-2">
                        Shipping total
                        <FaMoneyBill className="text-green-500"/>
                        <span className="font-bold"> ${order.amount}</span>
                    </div>
                </div>

                <div className="mt-4">
                    <div className="grid grid-cols-2 gap-4 text-sm text-gray-600">
                        <div>Order ID: <span className="font-medium">{order.orderId}</span></div>
                        <div>Date: <span className="font-medium">{formatDate(order.creationDate)}</span></div>
                        <div>Status: <span className="font-medium px-2 py-1 bg-blue-100 rounded-full">{order.status}</span>
                        </div>
                        <div>Payment: <span className="font-medium">{order.paymentType}</span></div>
                    </div>
                </div>

                <div className="mt-8">
                    <h2 className="text-xl font-semibold mb-4">Transactions</h2>
                    {order.bookTransactions.map((transaction, index) => (
                        <div key={index} className="mb-6 border rounded-lg p-4">
                            <div className="flex items-center gap-3 mb-4">
                                <img src={transaction.ownerProfile.avatar} alt="Owner"
                                     className="w-10 h-10 rounded-full"/>
                                <div>
                                    <div className="flex mt-2 justify-center">
                                        <div className="font-medium">
                                            {transaction.ownerProfile.username}
                                            <p className="inline-block ml-2 px-2 py-1 text-sm rounded-md bg-green-100 text-green-800">
                                                <GiBlackBook className="inline-block"/>&nbsp;{transaction.books.length}
                                            </p>
                                            <p className="inline-block px-2 ml-2 py-1 text-sm rounded-md bg-gray-100 text-green-800 hover:cursor-pointer hover:bg-gray-200"
                                               onClick={() => navigate(ROUTES.transaction.url.replace(':orderId', order?.orderId).replace(":txId", transaction.txId))}
                                            >
                                                <MdOpenInNew className="inline-block"/>&nbsp;details
                                            </p>
                                        </div>
                                    </div>
                                    <div className="text-sm text-gray-500">Shipping : ${transaction.amount}</div>
                                </div>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">

                                {transaction.books.map((book) => (
                                    <div key={book.id} className="flex gap-3">
                                        <div>
                                            <div className="font-medium
                                            hover:cursor-pointer underline
                                            hover:text-blue-500
                                            "
                                                onClick={() => navigate(`/book/${book.id}`)}
                                            > {book.title}</div>
                                        </div>
                                    </div>
                                ))}
                            </div>

                            <div className="flex gap-4 mt-4">
                                Status:
                                {(() => {
                                    const lastStep = transaction.steps.sort((a, b) => a.date.localeCompare(b.date)).reverse()[0];
                                    return <>
                                        <div className="flex items-center gap-2 text-sm">
                                            <StepIcon type={lastStep.type}/>
                                            <span>{loanWorkflowResolverBorrow(lastStep.type).interpret}</span>
                                            <span className="text-gray-500">
                                                {formatDate(lastStep.date)}
                                            </span>
                                        </div>
                                    </>;
                                })()}
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    </>;
}