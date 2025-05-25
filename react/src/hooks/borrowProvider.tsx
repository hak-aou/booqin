import {createContext, Dispatch, ReactNode, useContext, useEffect, useReducer} from "react";
import {useSessionMethods} from "./session/sessionContext.tsx";
import {Order} from "../model/Cart.ts";

export const borrowContext = createContext<Order[]>([]);
export function useOrders() {
    return useContext(borrowContext)
}
export type BorrowAction = {type: "set", orders: Order[]}
export const borrowDispatchContext = createContext<Dispatch<BorrowAction>>(() => {});
export function useOrdersDispatch() {
    return useContext(borrowDispatchContext)
}


export function borrowReducer(state: Order[], action: BorrowAction): Order[] {
    switch (action.type) {
        case "set": {
            return action.orders;
        }
        default:
            return state;
    }
}

export function BorrowProvider({children} : {children: ReactNode}) {
    const [borrows, borrowDispatch] = useReducer(borrowReducer, []);
    const sessionMethods = useSessionMethods();

    useEffect(() => {
        fetchOrders();
        const intervalId = setInterval(() => {
            fetchOrders();
        }, 4_000);
        return () => clearInterval(intervalId);
    }, [sessionMethods.session.isLogged]);

    const fetchOrders = () => {
        if(sessionMethods.session.isLogged) {
            sessionMethods.api.myBorrowTransactions().then((response) => {
                borrowDispatch({type: "set", orders: response});
            });
        }
    };

    return (
        <borrowContext.Provider value={borrows}>
            <borrowDispatchContext.Provider value={borrowDispatch}>
                {children}
            </borrowDispatchContext.Provider>
        </borrowContext.Provider>
    );
}