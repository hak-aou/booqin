

interface ConfirmationModalProps {
    title: string;
    message: string;
    onConfirm: () => void;
    onCancel: () => void;
}

export default function ConfirmationModal(
    {
        title,
        message,
        onConfirm,
        onCancel
    } : ConfirmationModalProps) {
    return <>
        <div className="fixed inset-0 z-50 flex items-center justify-center ">
            <div className="bg-white p-4 rounded-lg shadow-lg border-2 border-primary">
                {/*Title*/}
                <h2 className="text-lg font-semibold mb-4 text-gray-900">{title}</h2>

                {/* Description */}
                <p>{message}</p>

                <div className="flex justify-end mt-4">
                    <button className="px-4 py-2 bg-red-500
                    hover:bg-red-600
                    hover:cursor-pointer text-white rounded-lg mr-2"
                            onClick={onConfirm}>Confirm</button>

                    <button className="px-4 py-2 bg-gray-300
                    hover:bg-gray-400
                    hover:cursor-pointer text-gray-900 rounded-lg"
                            onClick={onCancel}>Cancel</button>
                </div>
            </div>
        </div>
    </>;
}
