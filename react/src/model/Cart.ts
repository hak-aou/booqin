import {BookInfo} from "./book.ts";
import {UserPublicInfo} from "./userPublicInfo.ts";

export interface CartBookItem {
    book: BookInfo;
    locked: boolean;
    lockedUntil: string;
}

export interface Cart {
    UUID: string;
    version: number;
    estimatedPrice: number;
    books: CartBookItem[];
}

export interface ChargeRequest {
    paymentMethodId: string;
    orderId: string;
    cartVersion: number;
}

export interface ChargeCompletion {
    paymentIntentId: any,
    orderId: string,
    cartVersion: number,
}


//-- Checkout
export interface Checkout {
    error: boolean;
    errorMessage: string;
    order: Order | null;
}

export interface Order {
    orderId: string
    userId: string
    cartVersion: number
    creationDate: string
    amount: number
    status: OrderStatus
    paymentType: string
    paymentTxId: string
    bookTransactions: Transaction[]
}

export interface Transaction {
    txId: string
    ownerId: string
    ownerProfile: OwnerProfile
    books: BookInfo[]
    amount: number
    steps: TransactionStep[]
}

export interface TransactionStep {
    id: number
    type: TransactionStepType
    date: any
}

export interface OwnerProfile {
    userId: string
    username: string
    avatar: string
}

export type OrderStatus = "FULFILLED" | "PENDING" | "CANCELLED" | "REFUNDED" | "EXPIRED"
export type TransactionStepType = "TO_BE_SENT" | "SENT" | "RECEIVED" | "RETURNED" | "RECEIVED_BACK"
//-- end checkout

export interface BookLendTransaction {
    txId: string;
    books: BookInfo[];
    steps: TransactionStep[];
    user: UserPublicInfo;
}

export interface NextStep {
    type: TransactionStepType;
    meaning: string
}

export interface WorkflowInterpretation {
    nextStep?: NextStep;
    interpret: string;
}

export const loanWorkflowResolverBorrow = (step: TransactionStepType): WorkflowInterpretation  => {
    switch (step) {
        case "TO_BE_SENT":
            return {interpret: "Awaiting shipment"};
        case "SENT":
            return {
                nextStep: {
                    type: "RECEIVED",
                    meaning: "Mark as received"
                } as NextStep,
                interpret: "In transit"
            };
        case "RECEIVED":
            return {
                nextStep: {
                    type: "RETURNED",
                    meaning: "Mark as shipped back"
                } as NextStep,
                interpret: "Received"
            };
        case "RETURNED":
            return { interpret: "Returning" };
        case "RECEIVED_BACK":
            return { interpret: "Returned" };
    }
}

export const loanWorkflowResolverLend = (step: TransactionStepType): WorkflowInterpretation => {
    switch (step) {
        case "TO_BE_SENT":
            return {interpret: "Waiting for you to send the books", nextStep: {type: "SENT", meaning: "Mark as sent"}};
        case "SENT":
            return {interpret: "Sent"};
        case "RECEIVED":
            return {interpret: "Received"};
        case "RETURNED":
            return {interpret: "Shipped back", nextStep: {type: "RECEIVED_BACK", meaning: "Mark as received back"}};
        case "RECEIVED_BACK":
            return {interpret: "Returned"};
    }
}