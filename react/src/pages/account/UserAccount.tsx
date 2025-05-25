import {useSession} from "../../hooks/session/sessionContext.tsx";
import {formatDate} from "../../utils/date.ts";

function Account() {
    const session = useSession()
    const authIdentity = session.loggedSession?.authIdentity
    const user = session.loggedSession?.user

    return (
        <>
            <div className="flex justify-center min-h-screen bg-gray-100">
                <div className="w-3/4 p-4 bg-white rounded-lg shadow-lg h-full">
                    <div className="flex items-center gap-4">
                        <img src={user?.imageUrl} alt="avatar" className="rounded-full w-10 h-10"/>
                        <h1 className="text-2xl font-semibold">
                            {session.loggedSession?.user?.username}
                            {session.loggedSession?.user?.isAdmin &&
                                <span className="text-orange-500"> (admin)</span>
                            }
                        </h1>
                        <h2>{authIdentity?.email}</h2>
                    </div>
                    {/* Account details */}
                    <div className="p-4">
                        <h1 className="text-orange-500 font-semibold">Account details</h1>
                        <div style={{textAlign: "left"}}>
                            <ul>
                                <li><b>Account id:</b> &nbsp;
                                    {user?.id}
                                </li>
                                <li>
                                    <b>On BooqIn since:</b> &nbsp;
                                    {formatDate(user?.creationDate)}
                                </li>
                            </ul>
                        </div>
                    </div>

                    {/* Account detail */}
                    <div className="p-4">
                        <h1 className="text-orange-500 font-semibold">My private info</h1>
                        <div style={{textAlign: "left"}}>
                            <ul>
                                <li><b>email:</b> &nbsp;
                                    {user?.email}
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </>
    )
}

export default Account