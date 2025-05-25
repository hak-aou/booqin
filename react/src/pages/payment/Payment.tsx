// react/src/pages/PaymentPage.tsx

import { useState } from 'react';
import {CreatePaymentMethodData, loadStripe} from '@stripe/stripe-js';
import { Elements, CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {ChargeRequest} from "../../model/Cart.ts";
import {useNavigate} from "react-router-dom";
import {ROUTES} from "../../routes/routes.ts";

const STRIPE_PUB_KEY = import.meta.env.VITE_STRIPE_PUB_KEY;
const stripePromise = loadStripe(STRIPE_PUB_KEY);

export interface PaymentFormProps {
    amount: number;
    orderId: string;
    cartVersion: number;
}

const PaymentForm = (props: PaymentFormProps) => {

    const stripe = useStripe();
    const elements = useElements();
    const [error, setError] = useState<string | undefined>(undefined);
    const [loading, setLoading] = useState<boolean>(false);
    const [success, setSuccess] = useState<boolean>(false);
    const sessionMethods = useSessionMethods();
    const navigate = useNavigate();

    const handleSubmit = async (event: any) => {
        event.preventDefault();
        setLoading(true);
        setError(undefined);

        if (!stripe || !elements) {
            setError('Stripe has not loaded yet.');
            setLoading(false);
            return;
        }

        const cardElement = elements.getElement(CardElement);
        const { error, paymentMethod } = await stripe.createPaymentMethod({
            type: 'card',
            card: cardElement,
        } as CreatePaymentMethodData);

        if (error) {
            setError(error.message || 'An error occurred while processing the payment.');
            setLoading(false);
        } else {
            try {
                const response = await sessionMethods.api.charge({
                    paymentMethodId: paymentMethod.id,
                    orderId: props.orderId,
                    cartVersion: props.cartVersion,
                } as ChargeRequest);

                if (response.status === "requires_confirmation") {
                    const { error } = await stripe.confirmCardPayment(response.clientSecret);
                    if (error) {
                        setError(error.message || 'An error occurred while processing the payment.');
                        setLoading(false);
                        return;
                    }
                }

                const completionResponse = await sessionMethods.api.completePayment(response.id);

                if (completionResponse || true) {
                    setSuccess(true);
                    setLoading(false);
                    setTimeout(() => {
                        navigate(ROUTES.order.url.replace(':orderId', props.orderId));
                    }, 2000);
                }
            } catch (err) {
                console.log(err)
                setError("An error occurred while processing the payment.");
                setLoading(false);
            }
        }
    };

    return (
        <form onSubmit={handleSubmit} className="max-w-md mx-auto space-y-6">
            <div className="mb-6">
                <h2 className="text-2xl font-bold text-gray-800 mb-4">Payment Details</h2>
                <div className="p-4 border rounded-md bg-white shadow-sm"
                    >
                    <CardElement/>
                </div>
            </div>

            <button
                type="submit"
                disabled={!stripe || loading}
                className={`w-full py-3 px-6 rounded-md text-white font-semibold hover:cursor-pointer
                    ${loading
                    ? 'bg-gray-400 cursor-not-allowed'
                    : 'bg-teal-600 hover:bg-teal-700 transition-colors'}
                `}
            >
                {loading ? (
                    <span className="flex items-center justify-center">
                        <svg className="animate-spin h-5 w-5 mr-3" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"/>
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2
                            5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"/>
                        </svg>
                        Processing...
                    </span>
                ) : (
                   <span>${props.amount}</span>
                )}
            </button>

            {error && (
                <div className="mt-4 p-3 bg-red-50 border border-red-200 text-red-600 rounded-md">
                    {error}
                </div>
            )}
            {success && (
                <div className="mt-4 p-3 bg-green-50 border border-green-200 text-green-600 rounded-md">
                    Payment successful! Redirecting...
                </div>
            )}
        </form>
    );
};

export interface PaymentForm {
    orderId: string;
    amount: number;
    cartVersion: number;
}

function Payment({orderId, amount, cartVersion}: PaymentForm) {
    return <>
        <Elements stripe={stripePromise}>
            <PaymentForm
                amount={amount}
                orderId={orderId}
                cartVersion={cartVersion}
            />
        </Elements>
    </>
}

export default Payment;