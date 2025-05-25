import {SubmitHandler, useForm} from "react-hook-form";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import {useState} from "react";
import {FilterBooksDTO} from "../../model/filter.ts";
import {CreateFilter} from "../../component/search/filter/FilterComponent.tsx";

interface CollectionCreationFormModalProps {
    onClose: () => void
}

interface CollectionFormInputs {
    title: string;
    description: string;
    visibility: boolean;
    smart: boolean;
}

export default function CollectionCreationFormModal(props: CollectionCreationFormModalProps) {
    const sessionMethods = useSessionMethods();
    const {register, handleSubmit, formState: {errors}} = useForm<CollectionFormInputs>();

    const [isSmart, setIsSmart] = useState(false);
    const [filter, setFilter] = useState<FilterBooksDTO>({
        title: undefined,
        hasSubtitle: undefined,
        categories: [],
        languages: [],
        authors: [],
        pageCountInterval: undefined,
        publishedDateInterval: undefined
    });

    const onSubmit: SubmitHandler<CollectionFormInputs> = async data => {
        console.log("data", data);
        console.log("filter", filter);
        if (data.smart) {
            await sessionMethods.api.createSmartCollection({
                title: data.title,
                description: data.description,
                visibility: data.visibility,
                filterBooksDTO: filter,
            })
        } else {
            await sessionMethods.api.createCollection({
                title: data.title,
                description: data.description,
                visibility: data.visibility
            })
        }
        sessionMethods.api.fetchAndUpdateMyCollections();
        props.onClose();
        /*navigate(ROUTES.collectionDetail.url.replace(':collectionId', ""+ collection.id))*/
    };

    return <>
        <div className="fixed top-0 left-0 w-full h-full flex items-center justify-center">
            <div className="bg-white rounded-lg p-4 border-2 border-gray-800">
                {/*Title*/}
                <h1 className="text-lg font-semibold">Create a new collection</h1>

                {/* Form */}
                <form onSubmit={handleSubmit(onSubmit)}>

                    {/* Title */}
                    <input
                        type="text"
                        placeholder="Title"
                        {...register("title", {required: true})}
                        className="w-full p-2 border border-gray-300 rounded-lg mt-2"
                    />
                    {errors.title && <span className="text-red-500">Title is required</span>}

                    {/* Description */}
                    <textarea
                        placeholder="Description"
                        {...register("description", {required: true})}
                        className="w-full p-2 border border-gray-300 rounded-lg mt-2"
                    />
                    {errors.description && <span className="text-red-500">Description is required</span>}

                    {/* Visibility */}
                    <div className="flex items-center mt-2">
                        <label className="inline-flex items-center cursor-pointer">
                            <input
                                type="checkbox"
                                {...register("visibility")}
                                className="sr-only peer"
                            />
                            <div
                                className="relative w-11 h-6 bg-gray-200 peer-focus:outline-none
                                rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full
                                rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white
                                after:content-[''] after:absolute after:top-[2px] after:start-[2px]
                                after:bg-white after:border-gray-300 after:border after:rounded-full
                                after:h-5 after:w-5 after:transition-all dark:border-gray-600
                                peer-checked:bg-teal-700 dark:peer-checked:bg-teal-700"
                            ></div>
                            <span className="ms-3 text-sm font-medium text-gray-900 dark:text-gray-300">Public</span>
                        </label>
                    </div>

                    {/* Smart */}
                    <div className="flex items-center mt-2">
                        <label className="inline-flex items-center cursor-pointer">
                            <input
                                type="checkbox"
                                {...register("smart", {
                                    onChange: (e) => setIsSmart(e.target.checked),
                                })}
                                className="sr-only peer"
                            />
                            <div
                                className="relative w-11 h-6 bg-gray-200 peer-focus:outline-none
                                rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full
                                rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white
                                after:content-[''] after:absolute after:top-[2px] after:start-[2px]
                                after:bg-white after:border-gray-300 after:border after:rounded-full
                                after:h-5 after:w-5 after:transition-all dark:border-gray-600
                                peer-checked:bg-teal-700 dark:peer-checked:bg-teal-700"
                            ></div>
                            <span
                                className="ms-3 text-sm font-medium text-gray-900 dark:text-gray-300">Smart collection</span>
                        </label>
                    </div>

                    {/* Smart collection filter */}
                    {isSmart && (
                        <>
                            <p className="font-semibold">Configure the filter for your Smart Collection:</p>
                            <CreateFilter
                                onChangeFilter={setFilter}/>

                        </>
                    )}

                    {/* Buttons */}
                    <div className="flex justify-end mt-2">
                        {/* Cancel */}
                        <button
                            type="button"
                            className="bg-red-500 text-white p-2 rounded-lg hover:cursor-pointer"
                            onClick={props.onClose}
                        >
                            Cancel
                        </button>

                        {/* Create */}
                        <button
                            type="submit"
                            className="bg-green-500 text-white p-2 rounded-lg ml-2 hover:cursor-pointer"
                        >Create
                        </button>
                    </div>
                </form>
            </div>
        </div>

    </>
}