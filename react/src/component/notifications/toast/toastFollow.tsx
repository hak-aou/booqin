import {FollowNotification} from "../../../model/notification.ts";
import {toast, ToastContentProps} from "react-toastify";

export const toastFollow = (notification: FollowNotification) => {
    const customToastComponent = ({ closeToast }: ToastContentProps) => {
        return (
            <div>
                <button
                    className="rounded-full absolute top-[-8px] left-[-6px] opacity-0 group-hover:opacity-100 transition-opacity  shadow-inner shadow-zinc-400 bg-zinc-700/70  size-5 grid place-items-center border border-zinc-400"
                    onClick={closeToast}
                >
                    <svg
                        aria-hidden="true"
                        viewBox="0 0 14 16"
                        className={'fill-white size-3'}
                    >
                        <path
                            fillRule="evenodd"
                            d="M7.71 8.23l3.75 3.75-1.48 1.48-3.75-3.75-3.75 3.75L1 11.98l3.75-3.75L1 4.48 2.48 3l3.75 3.75L9.98 3l1.48 1.48-3.75 3.75z"
                        />
                    </svg>
                </button>
                <p>{notification.username} started following you</p>
            </div>
        );
    }
    toast.info(customToastComponent, {
        className:
            'bg-zinc-900/40 backdrop-blur-lg shadow-inner border border-zinc-900/20 rounded-2xl text-white overflow-visible group mt-20',
        closeButton: false,
        pauseOnHover: true,
        hideProgressBar: false,
        closeOnClick: true,
        icon: false,
        progressClassName: 'bg-blue-500 color-blue-500',
        position: "bottom-left",
        autoClose: 5000,
    });
}