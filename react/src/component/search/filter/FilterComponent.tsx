import {useEffect, useState} from "react";
import {TabInformations} from "../../../model/search.ts";
import {
    FilterBooksDTO,
    IntervalDate,
    IntervalNumber
} from "../../../model/filter.ts";
import {SessionMethods, useSessionMethods} from "../../../hooks/session/sessionContext.tsx";
import {BookInfo} from "../../../model/book.ts";
import {PaginatedResult} from "../../../model/common.ts";

// https://flowbite.com/docs/forms/input-field/
// https://flowbite.com/docs/components/dropdowns/

function FilterDropdown(
    {
        title,
        options,
        onSelect,
    }: {
        title: string,
        options: string[],
        onSelect: (value: string) => void,
    }) {

    const [isOpen, setIsOpen] = useState(false);

    const toggleDropdown = () => setIsOpen(!isOpen);

    const closeDropdown = (index: number) => () => {
        onSelect(options[index]);
        setIsOpen(false);
    };

    return (
        <>
            <div className="relative" style={{overflow: "visible"}}>
                <button data-dropdown-toggle="dropdown"
                        className="cursor-pointer flex items-center justify-between px-4 py-2 w-30 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-full shadow-sm focus:outline-none focus:ring focus:ring-blue-400 focus:ring-opacity-50 hover:bg-gray-100"
                        type="button"
                        onClick={toggleDropdown}
                >

                    {title}

                    <svg className="w-2.5 h-2.5 ms-3"
                         xmlns="http://www.w3.org/2000/svg" fill="none"
                         viewBox="0 0 10 6">
                        <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                              d="m1 1 4 4 4-4"/>
                    </svg>
                </button>

                {/* Dropdown menu */}
                {isOpen && (
                    <div id="dropdown"
                         className="z-10  bg-white divide-y divide-gray-100 rounded-2xl shadow-sm w-30 dark:bg-gray-700">
                        <ul className="py-2 text-sm bg-white text-black rounded-lg max-h-60 overflow-y-auto"
                            aria-labelledby="dropdownDefaultButton">
                            {options.map((option, index) => (
                                <li key={index}>
                                    <a
                                        className="block px-4 py-2 bg-white text-black rounded-lg hover:bg-gray-100"
                                        onClick={closeDropdown(index)}
                                    >
                                        {option}
                                    </a>
                                </li>
                            ))}
                        </ul>
                    </div>
                )}
            </div>
        </>
    )
}

enum CheckboxState {
    TRUE,
    FALSE,
    UNDEFINED
}

function FilterCheckbox({
                            title,
                            onCheck,
                            isActive = undefined
                        }: {
    title: string;
    onCheck: (value: boolean | undefined) => void;
    isActive?: boolean;
}) {

    const [state, setState] = useState<CheckboxState>(CheckboxState.UNDEFINED);

    const handleChange = () => {
        let newState: CheckboxState;
        switch (state) {
            case CheckboxState.TRUE:
                newState = CheckboxState.FALSE;
                onCheck(false);
                break;
            case CheckboxState.FALSE:
                newState = CheckboxState.UNDEFINED;
                onCheck(undefined);
                break;
            case CheckboxState.UNDEFINED:
                newState = CheckboxState.TRUE;
                onCheck(true);
                break;
            default:
                newState = CheckboxState.UNDEFINED;
                onCheck(undefined);
                break;
        }
        setState(newState);
    };

    useEffect(() => {
        if (isActive !== undefined && isActive !== null) {
            setState(isActive ? CheckboxState.TRUE : CheckboxState.FALSE);
        } else {
            setState(CheckboxState.UNDEFINED);
        }
    }, [isActive]);

    const displayIcon = () => {
        switch (state) {
            case CheckboxState.TRUE:
                return (
                    <span
                        className="absolute bg-green-400 text-white top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2">
                    <svg className="w-4 h-4" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" width="24"
                         height="24" fill="none" viewBox="0 0 24 24">
                        <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                              d="M5 11.917 9.724 16.5 19 7.5"/>
                    </svg>
                </span>
                );

            case CheckboxState.FALSE:
                return (
                    <span
                        className="absolute bg-red-400 text-white top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2">
                        <svg className="w-4 h-4 text-gray-800 dark:text-white" aria-hidden="true"
                             xmlns="http://www.w3.org/2000/svg" width="24"
                             height="24" fill="none" viewBox="0 0 24 24">
                          <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                                d="M6 18 17.94 6M18 18 6.06 6"/>
                        </svg>

                </span>
                );

            default:
                return null;
        }
    };


    return (
        <div className="flex items-center">
            <label className="text-sm font-medium text-black self-center pr-2">
                {title}
            </label>
            <div className="inline-flex items-center">
                <label className="flex items-center cursor-pointer relative">
                    <input type="checkbox"
                           className="peer h-4 w-4 cursor-pointer transition-all appearance-none border-2 border-gray-300"
                           checked={state === CheckboxState.TRUE}
                           onChange={handleChange}
                    />
                    {displayIcon()}

                </label>
            </div>
        </div>
    );
}


function FilterInputField(
    {
        title,
        placeHolder,
        onSelect,
    }: {
        title: string,
        placeHolder: string,
        onSelect: (value: string) => void,
    }
) {
    const [value, setValue] = useState("");

    const handleSelect = () => {
        if (value) {
            onSelect(value);
            setValue("");
        }
    }

    return (
        <div className="flex flex-row gap-2 items-center justify-center ">
            <label className="text-sm font-medium text-black self-center">
                {title}
            </label>
            <form
                onSubmit={(e) => {
                    e.preventDefault();
                    handleSelect();
                }}
            >
                <div className="w-full max-w-sm min-w-[200px]">
                    <div className="relative">
                        <input
                            type="text"
                            className="w-60 p-2 bg-white border border-gray-300 text-black text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block"
                            placeholder={placeHolder}
                            onChange={(e) => setValue(e.target.value)}
                        />
                        <button
                            className="absolute top-1/2 right-1 transform -translate-y-1/2 flex items-center justify-center rounded bg-slate-800 p-1 border border-transparent text-sm text-white transition-all shadow-sm hover:shadow focus:bg-slate-700 active:bg-slate-700 disabled:pointer-events-none disabled:opacity-50 cursor-pointer"
                            type="button"
                            onClick={() => {
                                handleSelect()
                            }}
                        >
                            <svg className="w-4 h-4" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                                 viewBox="0 0 24 24">
                                <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                                      d="M5 12h14m-7 7V5"/>
                            </svg>
                        </button>

                    </div>
                </div>

            </form>
        </div>

    )
}

function FilterInterval(
    {
        title,
        firstPlaceholder,
        secondPlaceholder,
        onChange,
        hasDefaultValue = undefined
    }: {
        title: string,
        firstPlaceholder: string,
        secondPlaceholder: string,
        onChange: (interval: IntervalNumber | undefined) => void,
        hasDefaultValue?: IntervalNumber
    }
) {

    const [minValue, setMinValue] = useState("");
    const [maxValue, setMaxValue] = useState("");

    useEffect(() => {
        if (hasDefaultValue) {
            let min = hasDefaultValue?.min ? String(hasDefaultValue.min) : "";
            let max = hasDefaultValue?.max ? String(hasDefaultValue.max) : "";

            setMinValue(String(min))
            setMaxValue(String(max))
        }
    }, [hasDefaultValue])

    useEffect(() => {
        onChange({
            min: parseInt(minValue),
            max: parseInt(maxValue),
        })
    }, [minValue, maxValue])

    return (
        <div className="flex flex-row gap-2 items-center justify-center ">
            <label className="text-sm font-medium text-black self-center">
                {title}
            </label>
            <input
                type="text"
                className="bg-white border border-gray-300 text-black text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-15 p-2"
                placeholder={firstPlaceholder}
                value={minValue}
                onChange={(e) => setMinValue(e.target.value)}
            />
            <input
                type="text"
                className="bg-white border border-gray-300 text-black text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-15 p-2"
                placeholder={secondPlaceholder}
                value={maxValue}
                onChange={(e) => setMaxValue(e.target.value)}
            />
        </div>

    )
}

function DateIntervalPicker({
                                title,
                                onChange,
                                hasDefaultValue = undefined
                            }: {
    title: string,
    onChange: (interval: IntervalDate) => void,
    hasDefaultValue?: IntervalDate
}) {
    const [minDate, setMinDate] = useState(hasDefaultValue?.min ? new Date(hasDefaultValue.min).toISOString().split('T')[0] : "");
    const [maxDate, setMaxDate] = useState(hasDefaultValue?.max ? new Date(hasDefaultValue.max).toISOString().split('T')[0] : "");

    useEffect(() => {
        if (hasDefaultValue) {
            setMinDate(hasDefaultValue.min ? new Date(hasDefaultValue.min).toISOString().split("T")[0] : "");
            setMaxDate(hasDefaultValue.max ? new Date(hasDefaultValue.max).toISOString().split("T")[0] : "");
        }
    }, [hasDefaultValue]);


    useEffect(() => {
        const min = minDate ? new Date(minDate) : undefined;
        const max = maxDate ? new Date(maxDate) : undefined;

        onChange({
            min,
            max,
        });
    }, [minDate, maxDate]);

    return (
        <div className="flex flex-row gap-2 items-center justify-center">
            <label className="text-sm font-medium text-black self-center">
                {title}
            </label>

            <div className="flex items-center">
                <div className="relative">
                    <input
                        name="start"
                        type="date"
                        className="bg-white border border-gray-300 text-black text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2"
                        value={minDate}
                        onChange={(e) => setMinDate(e.target.value)}
                    />
                </div>

                <span className="mx-4 text-gray-500">to</span>

                <div className="relative">
                    <input
                        name="end"
                        type="date"
                        className="bg-white border border-gray-300 text-black text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2"
                        value={maxDate}
                        onChange={(e) => setMaxDate(e.target.value)}
                    />
                </div>
            </div>
        </div>
    );
}


function BookFilter({
                        collectionId,
                        sessionMethod,
                        localFilter,

                        onRefresh,
                        onChangeFilter,

                    }: {
    collectionId: number,
    sessionMethod: SessionMethods,
    localFilter?: FilterBooksDTO,

    onRefresh: (book: PaginatedResult<BookInfo>, newFilter: FilterBooksDTO) => void,
    onChangeFilter?: (filter: FilterBooksDTO) => void
}) {

    const [filter, setFilter] = useState<FilterBooksDTO>({
        title: undefined,
        hasSubtitle: undefined,
        categories: [],
        languages: [],
        authors: [],
        pageCountInterval: undefined,
        publishedDateInterval: undefined
    });

    const [categoriesOptions, setCategoriesOptions] = useState<string[]>([]);
    const [languagesOptions, setLanguagesOptions] = useState<string[]>([]);

    useEffect(() => {
        if (localFilter) {
            setFilter(localFilter);
        }
        console.log("LOCAL FILTER", localFilter)
    }, [localFilter]);

    useEffect(() => {
        async function fetchOptions() {
            try {
                const categories = await sessionMethod.api.categories();
                const languages = await sessionMethod.api.languages();
                setCategoriesOptions(categories);
                setLanguagesOptions(languages);
            } catch (error) {
                console.error("Error while fetching options", error);
            }
        }

        fetchOptions().then();
    }, [sessionMethod]);

    useEffect(() => {
        if (onChangeFilter) {
            onChangeFilter(filter);
        }
    }, [filter, onChangeFilter]);


    const refresh = async () => {
        try {
            if (!collectionId || !onRefresh) return;
            const books = await sessionMethod.api.filterBooks(collectionId, filter, {offset: 0, limit: 25});
            onRefresh(books, filter);
        } catch (err) {
            console.error("Error while filtering books", err);
        }
    };

    return (
        <FilterUI
            filter={filter}
            setFilter={setFilter}
            categoriesOptions={categoriesOptions}
            languagesOptions={languagesOptions}
            hideRefreshButton={false}
            onRefresh={refresh}
        />
    );
}

function TabFilter({
                       tab,
                       collectionId,
                       sessionMethod,
                       onRefresh
                   }: {
    tab: TabInformations,
    collectionId: number,
    sessionMethod: SessionMethods,
    onRefresh: (book: PaginatedResult<BookInfo>) => void;
}) {
    switch (tab) {
        case TabInformations.COLLECTIONS: {/* return <CollectionFilter/>*/
        }
            break;
        case TabInformations.BOOKS:
            return <BookFilter
                collectionId={collectionId}
                sessionMethod={sessionMethod}
                onRefresh={onRefresh}
            />
        case TabInformations.USERS: {/* return <UserFilter/> */
        }
            break;
    }
}


export function FilterUI({
                             filter,
                             setFilter,
                             categoriesOptions,
                             languagesOptions,
                             hideRefreshButton,
                             onRefresh,
                         }: {
    filter: FilterBooksDTO,
    setFilter: (filter: FilterBooksDTO) => void,
    categoriesOptions: string[],
    languagesOptions: string[],
    hideRefreshButton?: boolean,
    onRefresh?: () => void,

}) {

    return (
        <div className={"flex flex-col p-4 gap-4"}>
            {/* Filters */}
            <div className="grid gap-4 w-full grid-cols-1 md:grid-cols-[1fr_2fr_2fr]">
                {/* Col 1 : Dropdowns */}
                <div className="flex flex-col gap-4 items-center">
                    <FilterDropdown
                        title={"Categories"}
                        options={categoriesOptions}
                        onSelect={(value: string) => {
                            if (!filter.categories.includes(value) && value !== "") {
                                setFilter({
                                    ...filter,
                                    categories: [...filter.categories, value]
                                });
                            }
                        }}
                    />
                    <FilterDropdown
                        title={"Langages"}
                        options={languagesOptions}
                        onSelect={(value: string) => {
                            if (!filter.languages.includes(value) && value !== "") {
                                setFilter({
                                    ...filter,
                                    languages: [...filter.languages, value]
                                });
                            }
                        }}
                    />
                </div>

                {/* Col 2 : Inputs */}
                <div className="flex flex-col gap-4 items-center">
                    <FilterInputField
                        title={"Author"}
                        placeHolder={"Author"}
                        onSelect={(value: string) => {
                            if (!filter.authors.includes(value) && value !== "") {
                                setFilter({
                                    ...filter,
                                    authors: [...filter.authors, value]
                                });
                            }
                        }}
                    />
                    <FilterInputField
                        title={"Title"}
                        placeHolder={"Title"}
                        onSelect={(value: string) => setFilter({
                            ...filter,
                            title: value
                        })}
                    />
                </div>

                {/* Col 3 : Checkbox + dropdown */}
                <div className="flex flex-col gap-4 items-start">
                    <FilterCheckbox
                        title={"Has subtitle"}
                        onCheck={(value) => setFilter({
                            ...filter,
                            hasSubtitle: value
                        })}
                        isActive={filter.hasSubtitle}
                    />
                    <FilterInterval
                        title={"Page count"}
                        firstPlaceholder={"Min"}
                        secondPlaceholder={"Max"}
                        onChange={(interval) => setFilter({
                            ...filter,
                            pageCountInterval: interval
                        })}
                        hasDefaultValue={filter.pageCountInterval}
                    />
                    <DateIntervalPicker
                        title={"Published date"}
                        onChange={(interval) => setFilter({
                            ...filter,
                            publishedDateInterval: interval
                        })}
                        hasDefaultValue={filter.publishedDateInterval}
                    />
                </div>

            </div>

            {/* Options */}
            <div className="flex flex-row gap-4 w-full">
                {/* Choosen categories */}
                <div className="">
                    {filter.categories.map((category) => (
                        <div
                            key={`key-category-${category}`}
                            className="bg-orange-100 text-gray-700 pl-3 pr-1 py-1 rounded-full flex items-center gap-1 mb-1">
                            {category}
                            <button
                                onClick={() => setFilter({
                                    ...filter,
                                    categories: filter.categories.filter((cat) => cat !== category)
                                })}>
                                <svg className="w-6 h-6 text-black hover:text-red-500"
                                     aria-hidden="true"
                                     xmlns="http://www.w3.org/2000/svg"
                                     width="24"
                                     height="24"
                                     fill="none"
                                     viewBox="0 0 24 24">
                                    <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round"
                                          strokeWidth="2" d="M6 18 17.94 6M18 18 6.06 6"/>
                                </svg>
                            </button>
                        </div>
                    ))}
                </div>

                {/* Choosen languages */}
                <div className="">
                    {filter.languages.map((language) => (
                        <div
                            key={`key-language-${language}`}
                            className="bg-orange-100 text-gray-700 pl-3 pr-1 py-1 rounded-full flex items-center gap-1 mb-1">
                            {language}
                            <button
                                onClick={() => setFilter({
                                    ...filter,
                                    languages: filter.languages.filter((cat) => cat !== language)
                                })}>
                                <svg className="w-6 h-6 text-black hover:text-red-500"
                                     aria-hidden="true"
                                     xmlns="http://www.w3.org/2000/svg"
                                     width="24"
                                     height="24"
                                     fill="none"
                                     viewBox="0 0 24 24">
                                    <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round"
                                          strokeWidth="2" d="M6 18 17.94 6M18 18 6.06 6"/>
                                </svg>
                            </button>
                        </div>
                    ))}
                </div>

                {/* Choosen authors */}
                <div className="">
                    {filter.authors.map((author) => (
                        <div
                            key={`key-author-${author}`}
                            className="bg-orange-100 text-gray-700 pl-3 pr-1 py-1 rounded-full flex items-center gap-1 mb-1">
                            <p className={"text-black italic"}>author:</p>
                            <p>{author}</p>
                            <button
                                onClick={() => setFilter({
                                    ...filter,
                                    authors: filter.authors.filter((cat) => cat !== author)
                                })}>
                                <svg className="w-6 h-6 text-black hover:text-red-500"
                                     aria-hidden="true"
                                     xmlns="http://www.w3.org/2000/svg"
                                     width="24"
                                     height="24"
                                     fill="none"
                                     viewBox="0 0 24 24">
                                    <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round"
                                          strokeWidth="2" d="M6 18 17.94 6M18 18 6.06 6"/>
                                </svg>
                            </button>
                        </div>
                    ))}
                </div>

                {/* Choosen title */}
                <div className="">
                    {filter.title && (
                        <div
                            className="bg-orange-100 text-gray-700 pl-3 pr-1 py-1 rounded-full flex items-center gap-1 mb-1">
                            <p className={"text-black italic"}>title:</p>
                            <p>{filter.title}</p>

                            <button
                                onClick={() => setFilter({
                                    ...filter,
                                    title: undefined
                                })}>
                                <svg className="w-6 h-6 text-black hover:text-red-500"
                                     aria-hidden="true"
                                     xmlns="http://www.w3.org/2000/svg"
                                     width="24"
                                     height="24"
                                     fill="none"
                                     viewBox="0 0 24 24">
                                    <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round"
                                          strokeWidth="2" d="M6 18 17.94 6M18 18 6.06 6"/>
                                </svg>
                            </button>
                        </div>
                    )}
                </div>

            </div>

            {!hideRefreshButton && (
                <button
                    className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 rounded-3xl w-20 cursor-pointer"
                    onClick={onRefresh}>
                    Refresh
                </button>
            )
            }


        </div>
    )

}

function CreateFilter({
                          onChangeFilter,
                      }: {
    onChangeFilter: (filter: FilterBooksDTO) => void;
}) {
    const sessionMethods = useSessionMethods();

    const [filter, setFilter] = useState<FilterBooksDTO>({
        title: undefined,
        hasSubtitle: undefined,
        categories: [],
        languages: [],
        authors: [],
        pageCountInterval: undefined,
        publishedDateInterval: undefined
    });

    const [categoriesOptions, setCategoriesOptions] = useState<string[]>([]);
    const [languagesOptions, setLanguagesOptions] = useState<string[]>([]);

    useEffect(() => {
        async function fetchOptions() {
            try {
                const categories = await sessionMethods.api.categories();
                const languages = await sessionMethods.api.languages();
                setCategoriesOptions(categories);
                setLanguagesOptions(languages);
            } catch (error) {
                console.error("Error while fetching options", error);
            }
        }

        fetchOptions().then();
    }, []);

    useEffect(() => {
        onChangeFilter(filter);
    }, [filter]);

    return (
        <FilterUI
            filter={filter}
            setFilter={setFilter}
            categoriesOptions={categoriesOptions}
            languagesOptions={languagesOptions}
            hideRefreshButton={true}
        />
    );

}


export {
    TabFilter,
    BookFilter,
    CreateFilter
}

