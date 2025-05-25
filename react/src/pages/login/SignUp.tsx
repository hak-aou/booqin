import { useForm, SubmitHandler } from "react-hook-form";
import { useSessionMethods } from "../../hooks/session/sessionContext.tsx";
import {useEffect, useState} from "react";
import { ROUTES } from "../../routes/routes.ts";
import { useNavigate } from "react-router-dom";
import getRandomPassword from 'diceware-password-generator';
import {RiRefreshLine} from "react-icons/ri";
import {MdCopyAll} from "react-icons/md";
import {SignUpForm} from "../../model/userPublicInfo.ts";

type FormValues = {
    username: string;
    email: string;
};

function SignUp() {
    const sessionState = useSessionMethods();
    const navigate = useNavigate();
    const { register, handleSubmit, formState: { errors }, setError } = useForm<FormValues>();
    const [passphrase, setPassphrase] = useState<string[]>([]);
    const [copied, setCopied] = useState<boolean>(false);

    const [success, setSuccess] = useState<boolean>(false);
    const [globalError, setGlobalError] = useState<string | null>(null);

    const generateNewPassphrase = () => {
        const pass = getRandomPassword({'format': 'array'});
        setPassphrase(pass);
        console.log(pass);
    };

    useEffect(() => {
        if (sessionState.isLogged) {
            navigate(ROUTES.home.url);
        }
        generateNewPassphrase();
    }, [sessionState.isLogged, navigate]);

    const onSubmit: SubmitHandler<FormValues> = (data) => {
        // check email
        if (!data.email.match(/^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/)) {
            setError("email", { type: "manual", message: "Invalid email" });
            return;
        }
        // check that username only contains numbers letters and underscores or hyphens
        if (!data.username.match(/^[a-zA-Z0-9_-]+$/)) {
            setError("username", { type: "manual", message: "Username can only contain letters, numbers, underscores and hyphens" });
        }
        if(data.username.length < 5) {
            setError("username", { type: "manual", message: "Username must be at least 5 characters long" });
        }
        sessionState.api.signUp({
            username: data.username,
            email: data.email,
            phrase: passphrase
        } as SignUpForm).then(() => {
            setSuccess(true);
        }).catch((error: any) => {
            setGlobalError(error.response.data.message);
            setSuccess(false);
        });
    };

    if (success) {
        // wait 3 seconds before redirecting to the login
        setTimeout(() => {
            navigate(ROUTES.login.url);
        }, 3000);
        return <div className="flex justify-center min-h-screen">
            <div className="w-3/4 p-4 rounded-lg h-full mt-25">
                <div className="bg-white shadow-lg rounded px-8 pt-6 pb-8 mb-4">
                    <div className="text-center">
                        <h2 className="text-2xl font-bold">Account created successfully</h2>
                        <p className="text-lg">You will be redirected to the login page..</p>
                    </div>
                </div>
            </div>
        </div>;
    }

    function copyToClipboard() {
        navigator.clipboard.writeText(passphrase.join("-"));
        setCopied(true);
        setTimeout(() => {
            setCopied(false);
        }, 3000);
    }

    return (
        <div className="flex justify-center min-h-screen">
            <div className="w-4/4 p-4 rounded-lg h-full mt-25">
                <form className="w-full max-w-sm mx-auto bg-white shadow-lg rounded px-8 pt-6 pb-8 mb-4" onSubmit={handleSubmit(onSubmit)}>
                    <div className="md:flex items-center mb-6">
                        <div className="md:w-1/3">
                            <label className="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4" htmlFor="usernameOrEmail">
                                Username
                            </label>
                        </div>
                        <div className="md:w-2/3">
                            <input
                                className="bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full py-2 px-4 text-gray-700 leading-tight focus:outline-none focus:bg-white focus:border-teal-500"
                                id="username"
                                placeholder="Username"
                                type="text"
                                {...register("username", { required: "This field is required" })}
                            />
                            {errors.username && <p className="text-red-500 text-xs italic">{errors.username.message}</p>}
                        </div>
                    </div>
                    <div className="md:flex items-center mb-6">
                        <div className="md:w-1/3">
                            <label className="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4" htmlFor="usernameOrEmail">
                                Email
                            </label>
                        </div>
                        <div className="md:w-2/3">
                            <input
                                className="bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full py-2 px-4 text-gray-700 leading-tight focus:outline-none focus:bg-white focus:border-teal-500"
                                id="email"
                                placeholder="your@email.com"
                                type="text"
                                {...register("email", { required: "This field is required" })}
                            />
                            {errors.email && <p className="text-red-500 text-xs italic">{errors.email.message}</p>}
                        </div>
                    </div>
                    <div className="md:w-1/3  mb-2">
                        <label className="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4" htmlFor="password">
                            Passphrase
                        </label>
                    </div>
                    <div className="md:w-full">
                        <div className="flex items-center">
                            <div
                                onClick={copyToClipboard}
                                className="
                                    hover:cursor-pointer
                                bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:bg-white focus:border-teal-500"
                            >
                              <span className="flex items-center">
                                    <span>
                                        {passphrase.join("-")}
                                    </span>
                                    <span className="text-teal-600 hover:text-teal-800 hover:cursor-pointer
                                 rounded hover:bg-gray-300 p-3">
                                        <MdCopyAll />
                                    </span>
                                </span>
                            </div>
                            <button
                                type="button"
                                onClick={generateNewPassphrase}
                                className="text-teal-600 hover:text-teal-800 hover:cursor-pointer
                                focus:outline-none focus:shadow-outline p-4 rounded hover:bg-gray-50 ml-2"
                            >
                                <RiRefreshLine />
                            </button>
                        </div>
                        {copied && <div className="text-xs text-green-500 italic mt-2">Copied to clipboard</div>}
                    </div>
                    <div className="text-xs text-gray-500 italic mt-2">
                        <p>Remember to store your passphrase in a safe place</p>
                    </div>
                    <div className="md:flex md:items-center mt-8">
                        <div className="md:w-1/3"></div>
                        <div className="md:w-2/3">
                            <button
                                className="shadow bg-teal-600 hover:bg-teal-500 hover:cursor-pointer focus:shadow-outline focus:outline-none text-white font-bold py-2 px-4 rounded"
                                type="submit">
                                Sign up
                            </button>
                        </div>
                    </div>
                    {globalError && <>
                        <div className="md:flex md:items-center mt-4 text-red-500 font-semibold text-xs italic">
                            <div className="md:w-1/3"></div>
                            <div className="md:w-2/3">
                                {globalError}
                            </div>
                        </div>
                    </>}

                </form>
            </div>
        </div>
    );
}

export default SignUp;