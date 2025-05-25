import {useNavigate} from "react-router-dom";
import {GiBlackBook} from "react-icons/gi";
import {useOrders} from "../../hooks/borrowProvider.tsx";
import {formatDateWithTime} from "../../utils/date.ts";
import {ROUTES} from "../../routes/routes.ts";

export function OrdersView() {
    const navigate = useNavigate();
    const orders = useOrders();

    return <>
        {orders
            .sort((a, b) => a.creationDate.localeCompare(b.creationDate)).reverse()
            .map((order) => {
                const totalBooks = order.bookTransactions.map(tx => tx.books.length).reduce((a, b) => a + b);
                return <div className=" border-b pb-4 bg-blue bg-white p-5 hover:cursor-pointer hover:bg-gray-50 mt-3"
                            key={order.orderId}
                            onClick={() => navigate(ROUTES.order.url.replace(":orderId", order.orderId))}>
                    <div className="mt-3 text-sm text-gray-600 flex justify-between">
                        <div>
                            <span className="font-medium">{formatDateWithTime(order.creationDate)}</span>
                            <div>
                                <span className="font-medium italic">
                                    {order.bookTransactions
                                        .flatMap(tx => tx.books).map(b=>b.title)
                                        .reduce((t1, t2)=>t1 + ', ' + t2)
                                        .slice(0, 50)}...
                                </span>
                            </div>
                        </div>
                        {/*{order.bookTransactions.map((tx: Transaction, index) => {
                            return  <div className="flex items-center " key={tx.txId}>
                                        <img src={tx.ownerProfile.avatar} alt="avatar"
                                             className={`rounded-full w-10 h-10 border-1 border-black bg-white ${index > 0 ? 'ml-[-15px]' : ''}`}/>

                                    </div>
                        })}*/}
                        <div>
                            {/*<span className="font-medium px-2 py-1 bg-blue-100 rounded-full">{order.status}</span>*/}
                            <div className="flex items-center justify-center mt-2">
                                <p className={`inline-block px-2 py-1 text-sm rounded-md bg-green-100 text-green-800`}>
                                    <GiBlackBook className="inline-block"/>&nbsp;{totalBooks}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            })}

    </>
}