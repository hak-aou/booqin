import {useParams} from "react-router";
import ErrorPage from "../../pages/ErrorPage/ErrorPage.tsx";
import {useSessionMethods} from "../../hooks/session/sessionContext.tsx";
import TabDisplayResult from "./TabDisplayResult.tsx";


function SearchResultHome() {
    const sessionMethods = useSessionMethods();
    const {searchData} = useParams();

    if (!searchData) return <ErrorPage title={"Incorrect search input"}
                                       errorDescription={"Incorrect search input"}/>;

    return <>
        <div className="flex justify-center min-h-screen bg-gray-100">
            <div className="w-3/4 p-4 bg-white rounded-lg shadow-lg h-full">
                <TabDisplayResult
                    sessionMethods={sessionMethods}
                    searchData={searchData}
                />
            </div>
        </div>
    </>
}

export default SearchResultHome;