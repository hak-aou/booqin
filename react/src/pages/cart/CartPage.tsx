import {useEffect, useRef, useState} from "react";
import {useNavigate} from "react-router-dom";
import {IoIosRemoveCircleOutline} from "react-icons/io";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {Cart, Checkout} from "../../model/Cart.ts";
import Payment from "../payment/Payment.tsx";
import {formatTimeLeft} from "../../utils/date.ts";
import {OrdersView} from "./OrdersView.tsx";

export default function CartPage() {
    const navigate = useNavigate();
    const sessionMethods = useSessionMethods();
    const [cart, setCart] = useState<Cart>({UUID: '', version: 0, estimatedPrice: 0, books: []});
    const [formatedDates, setFormatedDates] = useState<string[]>([]);
    const [checkingOut, setCheckingOut] = useState<boolean>(false);
    // Use ref to track latest cart value
    const cartRef = useRef(cart);

    // Update ref whenever cart changes
    useEffect(() => {
        cartRef.current = cart;
    }, [cart]);

    const fetchCart = () => {
        sessionMethods.api.getCart().then((response: Cart) => {
            setCart(response);
            /*// Format dates immediately after setting the cart with fresh data
            const dates = response.books.map((item) => formatTimeLeft(item.lockedUntil));
            setFormatedDates(dates);
            console.debug(response);*/
        });
    };

    useEffect(() => {
        fetchCart(); // Initial fetch

        // Set up intervals
        const cartUpdateInterval = setInterval(fetchCart, 8000);
        const dateUpdateInterval = setInterval(() => {
            // Access the current cart value from ref
            const dates = cartRef.current.books.map((item) => formatTimeLeft(item.lockedUntil));
            setFormatedDates(dates);
        }, 1000);

        return () => {
            clearInterval(cartUpdateInterval);
            clearInterval(dateUpdateInterval);
        };
    }, []);

    return <>
        <div className="flex flex-col md:flex-row justify-center min-h-screen">
            <div className="order-2 md:order-1 w-full md:w-1/4 p-4 bg-white rounded-lg shadow-lg">
                Past orders
                <OrdersView/>
            </div>
            <div className="order-1 md:order-2 w-full md:w-3/4 p-4 bg-white rounded-lg shadow-lg">
                {!checkingOut && <>
                    <h1 className="text-2xl font-bold mb-4 text-center">
                        Your cart {cart.books.length == 0 ? `is empty` : ""}
                    </h1>
                    <div className="space-y-4">
                        {cart.books
                            .sort((a, b) => a.book.title.localeCompare(b.book.title))
                            .map((item, index) => (
                                <div key={index} className="flex items-center justify-between p-4 rounded-lg shadow-md
                                    hover:cursor-pointer hover:bg-gray-50"
                                     onClick={() => navigate(`/book/${item.book.id}`)}
                                >
                                    <div className="flex items-center gap-4">
                                        <img src={item.book.imageLinks.small} alt={item.book.title} className="w-16 h-16 rounded"/>
                                        <div>
                                            <h2 className="text-xl font-semibold">{item.book.title}</h2>
                                            <p className="text-gray-600">{item.book.subtitle}</p>
                                        </div>
                                    </div>
                                    <div>
                                        {item.locked && <>
                                            <p className="text-primary">
                                                Guaranteed for {formatedDates[index]}
                                            </p>
                                        </>}
                                    </div>
                                    <div className="flex items-center gap-8 text-xs p-2">
                                        <button className="text-red-500 px-4 py-2 rounded-lg hover:text-red-600 hover:cursor-pointer p-3 hover:bg-gray-200"
                                                onClick={(event) => {
                                                    event.stopPropagation();
                                                    sessionMethods.api.removeBookFromCart(item.book.id).then(fetchCart)
                                                }}
                                        >
                                            <IoIosRemoveCircleOutline className="text-2xl"/>
                                        </button>
                                    </div>
                                </div>
                            ))}
                    </div>
                    {cart.books.length > 0 && <>
                        <div className="mt-4">
                            <h2 className="text-xl font-bold">Estimated shipping costs: ${cart.estimatedPrice.toFixed(2)}</h2>
                        </div>
                        <div className="flex justify-end mt-4">
                            <button className="bg-primary text-white px-4 py-2 rounded-lg hover:bg-primary-dark hover:cursor-pointer"
                                    onClick={() => setCheckingOut(true)}
                            >
                                Checkout
                            </button>
                        </div>
                    </>}
                </>}
                {checkingOut &&
                    <CheckoutView
                        cart={cart}
                        goBack={() => setCheckingOut(false)}
                    />
                }
            </div>
        </div>
    </>
}

interface CheckoutProps {
    cart: Cart;
    goBack: () => void;
}

function CheckoutView(props: CheckoutProps) {
    const sessionMethods = useSessionMethods();
    const [checkout, setCheckout] = useState<Checkout>({error: false, errorMessage: "", order: null});

    useEffect(() => {
        sessionMethods.api.checkout(props.cart.version).then((checkout) => {
            setCheckout(checkout);
            //console.log(checkout);
        })
    }, []);

    if (checkout.error) {
        return <div>Error: {checkout.errorMessage}</div>;
    }

    if (!checkout.order) {
        return <div>Loading...</div>;
    }

    const totalAmount = checkout.order.amount;

    return <>
        <div>
            <h1 className="text-2xl font-bold mb-4">Checkout</h1>
            <div className="space-y-4">
                {checkout.order.bookTransactions.map((transaction, index) => (
                    <div key={index} className="flex items-center justify-between p-4 rounded-lg shadow-md">
                        <div className="flex items-center gap-4">
                            <img src={transaction.ownerProfile.avatar} className="w-10 h-10 rounded" alt="avatar"/>
                            <div>
                                <h2 className="text-xl font-semibold">{transaction.ownerProfile.username}</h2>
                                <ul>
                                    {transaction.books.map((book, bookIndex) => (
                                        <li key={bookIndex}>{book.title}</li>
                                    ))}
                                </ul>
                            </div>
                        </div>
                        <div className="text-right">
                            <p className="text-md">${transaction.amount.toFixed(2)}</p>
                        </div>
                    </div>
                ))}

            </div>
            <div className="mt-4">
                <h2 className="text-xl text-right pr-3">Shipping:  ${totalAmount.toFixed(2)}</h2>
            </div>
            <div className="flex justify-between mt-4">
                <button className="bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 hover:cursor-pointer"
                        onClick={() => props.goBack()}
                >
                    Return to Cart
                </button>
            </div>
            <Payment
                orderId={checkout.order.orderId}
                amount={checkout.order.amount}
                cartVersion={checkout.order.cartVersion}
            />
        </div>
    </>
}