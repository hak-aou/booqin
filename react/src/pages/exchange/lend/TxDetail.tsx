import {
    BookLendTransaction,
    loanWorkflowResolverBorrow,
    loanWorkflowResolverLend
} from "../../../model/Cart.ts";
import {useNavigate, useParams} from "react-router-dom";
import {useSessionMethods} from "../../../hooks/session/sessionContext.tsx";
import {useEffect, useState} from "react";
import LendNav from "./LendNav.tsx";
import {GiBlackBook} from "react-icons/gi";
import {GoDotFill} from "react-icons/go";
import {formatDate} from "../../../utils/date.ts";
import {StepIcon} from "../../cart/OrderDetails.tsx";
import {TbFileBarcode} from "react-icons/tb";

export default function TxDetail() {
    const { txId } = useParams<{txId: string}>();
    const navigate = useNavigate();
    const sessionMethods = useSessionMethods();
    const [tx, setSelectedTx] = useState<BookLendTransaction | undefined>(undefined);

    useEffect(() => {
        fetchLendTransactions();
        const intervalId = setInterval(() => {
            fetchLendTransactions();
        }, 4_000);
        return () => clearInterval(intervalId);
    }, [sessionMethods.session.isLogged, txId]);

    const fetchLendTransactions = () => {
        if(sessionMethods.session.isLogged) {
            sessionMethods.api.myLendTransactions().then((response) => {
                if(txId) {
                    setSelectedTx(response.find(tx => tx.txId === txId));
                }
            });
        }
    };

    if(tx === undefined) {
        return <>Loading...</>;
    }

    const steps = tx.steps.sort((a, b) => a.date.localeCompare(b.date))

    const latestStep = steps[steps.length - 1];

    const todo = loanWorkflowResolverLend(latestStep.type);

    return <>
        <LendNav view={"Loans"}/>
        <div className="flex justify-center min-h-screen bg-gray-100">
            <div className="w-3/4 bg-gray rounded-lg shadow-lg h-full">
                <div className="p-2 bg-white">
                    <div className="mb-6 border border-gray-300 rounded-lg p-4">
                        <div className="flex items-center gap-3 mb-4">
                            <img src={tx.user.imageUrl} alt="Owner"
                                 className="w-10 h-10 rounded-full"/>
                            <div>
                                <div className="flex mt-2 justify-center">
                                    <div className="font-medium">
                                        {tx.user.username}
                                        <p className="inline-block ml-2 px-2 py-1 text-sm rounded-md bg-green-100 text-green-800">
                                            <GiBlackBook className="inline-block"/>&nbsp;{tx.books.length}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                            {tx.books.map((book) => (
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

                        <div className="mt-4 mt-9">
                            <h3 className="text-lg font-medium mb-2 "></h3>
                            <div className="flex flex-col gap-2 text-sm text-center pl-10 rounded-md bg-gray-50 p-4 shadow-inner inset-shadow-indigo-500">
                                {steps.map((step, index) => {
                                    const isCurrentStep = step.id === latestStep.id;
                                    const stepClass = isCurrentStep ? "text-blue-600 text-lg" : "text-gray-500 text-sm";
                                    return (
                                        <div>
                                            <div className="pl-1">
                                                {index > 0 && <div className="border-l-2 border-gray-300 h-5"/>}
                                            </div>
                                            <div key={step.id} className={`flex items-center gap-2 ${stepClass}`}>
                                                <GoDotFill className="text-gray-400"/>
                                                <span className="text-gray-400 text-xs">{formatDate(step.date)}</span>
                                                <div className="flex items-center gap-2">
                                                    <StepIcon type={step.type} className={stepClass}/>
                                                    <span>{loanWorkflowResolverBorrow(step.type).interpret}</span>
                                                </div>
                                            </div>
                                        </div>
                                    )
                                })}
                            </div>
                            {todo.nextStep && txId &&
                                <div className="flex flex-col gap-2 text-sm text-center pl-10">
                                    <div className="flex items-center gap-2 text-lg mt-9">
                                        <button
                                            className="bg-primary text-white px-4 py-2
                                            rounded-lg hover:cursor-pointer hover:bg-secondary"
                                            onClick={() => {
                                                sessionMethods.api.processTransactionNextStep(txId).then(() => {
                                                    fetchLendTransactions();
                                                })
                                            }}
                                        >
                                            <span>{todo.nextStep.meaning}</span>
                                        </button>
                                    </div>
                                </div>
                            }
                            {todo.nextStep && todo.nextStep.type === "SENT" &&
                                <button
                                    type="button"
                                    className="text-gray-900 bg-white hover:bg-gray-100 border
                                mt-5 hover:cursor-pointer
                                border-primary focus:ring-4 focus:outline-none focus:ring-gray-100
                                font-medium rounded-lg text-sm px-5 py-2.5 text-center inline-flex
                                 items-center
                                  me-2 mb-2"
                                    onClick={() => sessionMethods.api.getShippingLabel(txId!)
                                        .then((blob: Blob) => {
                                            const url = window.URL.createObjectURL(blob);
                                            const a = document.createElement('a');
                                            a.href = url;
                                            a.download = `shipping-label-${txId}.pdf`;
                                            document.body.appendChild(a);
                                            a.click();
                                            document.body.removeChild(a);
                                            window.URL.revokeObjectURL(url);
                                        }).catch(error => console.error('Error downloading the PDF:', error))
                                    }>
                                    <TbFileBarcode className="mr-2"/>
                                    <span>Get Shipping Label</span>
                                </button>
                            }
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </>
}