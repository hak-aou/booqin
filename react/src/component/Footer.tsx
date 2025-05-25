import { FaMastodon, FaGitlab, FaFacebookF, FaInstagram } from 'react-icons/fa';
import {FcAndroidOs} from "react-icons/fc";



function Footer(){
    return (
        <footer className="p-4 bg-white md:p-8 lg:p-10 dark:bg-gray-800">
            <div className="mx-auto max-w-screen-xl text-center">
                <p className="my-6 text-gray-500 dark:text-gray-400">
                    Projet Master 2 LID
                </p>
                <ul className="flex flex-wrap justify-center items-center mb-6 text-gray-900 dark:text-white">
                    <li>
                        <a href="#" className="mr-4 hover:underline md:mr-6 ">About</a>
                    </li>
                    <li>
                        <a href="#" className="mr-4 hover:underline md:mr-6">Blog</a>
                    </li>
                    <li>
                        <a href="#" className="mr-4 hover:underline md:mr-6">FAQs</a>
                    </li>
                    <li>
                        <a href="/booqin" className="mr-4 hover:underline md:mr-6">Who we are</a>
                    </li>
                </ul>

                {/* Social Media Icons */}
                <div className="flex justify-center space-x-6 mb-6">
                    <a href="#" target="_blank" rel="noopener noreferrer" className="text-gray-600 hover:text-blue-500 dark:text-gray-400 dark:hover:text-white">
                        <FaMastodon className="w-5 h-5" />
                        <span className="sr-only">Mastodon page</span>
                    </a>
                    <a href="https://gitlab.com/4nt0ineB/booqin-bastos" target="_blank" rel="noopener noreferrer" className="text-gray-600 hover:text-orange-500 dark:text-gray-400 dark:hover:text-white">
                        <FaGitlab className="w-5 h-5" />
                        <span className="sr-only">GitLab page</span>
                    </a>
                    <a href="https://gitlab.com/4nt0ineB/booqin-android" target="_blank" rel="noopener noreferrer" className="text-gray-600 hover:text-orange-500 dark:text-gray-400 dark:hover:text-white">
                        <FcAndroidOs className="w-5 h-5" />
                        <span className="sr-only">GitLab page</span>
                    </a>

                    <a href="#" target="_blank" rel="noopener noreferrer" className="text-gray-600 hover:text-blue-600 dark:text-gray-400 dark:hover:text-white">
                        <FaFacebookF className="w-5 h-5" />
                        <span className="sr-only">Facebook page</span>
                    </a>
                    <a href="#" target="_blank" rel="noopener noreferrer" className="text-gray-600 hover:text-pink-600 dark:text-gray-400 dark:hover:text-white">
                        <FaInstagram className="w-5 h-5" />
                        <span className="sr-only">Instagram page</span>
                    </a>
                </div>

                <span className="text-sm text-gray-500 sm:text-center dark:text-gray-400"> 2025 -
                    <a href="#"
                       className="hover:underline">&nbsp;BooqIn</a>. No right reserved.</span>
            </div>
        </footer>
    );
};

export default Footer;

