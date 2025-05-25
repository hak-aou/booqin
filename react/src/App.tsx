import './App.scss'
import {SessionProvider} from "./hooks/session/sessionProvider.tsx";
import Login from "./pages/login/Login.tsx";
import { Route, BrowserRouter as Router, Routes } from "react-router-dom";
import {ROUTES} from "./routes/routes.ts";
import ErrorPage from "./pages/ErrorPage/ErrorPage.tsx";
import Logout from "./pages/logout/Logout.tsx";
import ProtectedRoute from './routes/ProtectedRoute.tsx';
import UserAccount from "./pages/account/UserAccount.tsx";
import PublicProfile from "./pages/profile/PublicProfile.tsx";
import BookPage from "./pages/book/BookPage.tsx";
import PrivateProfile from "./pages/profile/PrivateProfile.tsx";
import {NotificationsCenter} from "./component/notifications/NotificationsCenter.tsx";
import Collections from "./pages/collections/Collections.tsx";
import CollectionDetail from "./pages/collections/CollectionDetail.tsx";
import {ToastContainer} from "react-toastify";
import Notifications from "./pages/Notifications.tsx";
import OrderDetails from "./pages/cart/OrderDetails.tsx";
import Lend from "./pages/exchange/lend/lend.tsx";
import {BorrowProvider} from "./hooks/borrowProvider.tsx";
import {WaitlistView} from "./pages/exchange/borrow/WaitlistView.tsx";
import {TransactionsView} from "./pages/exchange/borrow/TransactionsView.tsx";
import {OrdersView} from "./pages/cart/OrdersView.tsx";
import Navbar from "./component/navbar/Navbar.tsx";
import Tx from "./pages/exchange/borrow/Tx.tsx";
import {LendTransactionsView} from "./pages/exchange/lend/LendTransactionsView.tsx";
import TxDetail from "./pages/exchange/lend/TxDetail.tsx";
import CartPage from "./pages/cart/CartPage.tsx";
import SearchResultHome from "./component/search/SearchResultHome.tsx";
import SignUp from "./pages/login/SignUp.tsx";
import Administration from "./pages/admin/administration.tsx";
import Footer from "./component/Footer.tsx";

function App() {
  return <>
      <main className="max-w-7xl mx-auto">
          <SessionProvider>
              <BorrowProvider>
              <ToastContainer/>
              <NotificationsCenter />

                  <Router>
                      <Navbar/>
                      <Routes>
                          {/* ATTENTION, order matters */}
                          {/* Public Routes */}
                          <Route path={"/spa"} element={<Collections/>}/>
                          <Route path={ROUTES.home.url} element={<Collections/>}/>
                          <Route path={ROUTES.signUp.url} element={<SignUp/>}/>
                          <Route path={ROUTES.login.url} element={<Login previous={ROUTES.home.url}/>}/>
                          <Route path={ROUTES.logout.url} element={<Logout previous={ROUTES.home.url}/>}/>
                          <Route path={ROUTES.collectionDetail.url} element={<CollectionDetail/>}/>
                          <Route path={ROUTES.collections.url} element={<Collections/>}/>
                          <Route path={ROUTES.publicProfile.url} element={<PublicProfile/>}/>
                          <Route path={ROUTES.books.url} element={<BookPage/>}/>
                          <Route path={ROUTES.search.url} element={<SearchResultHome/>}/>

                          {/* Protected Routes */}
                          <Route path={ROUTES.admin.url} element={<ProtectedRoute> <Administration/> </ProtectedRoute>}/>
                          <Route path={ROUTES.borrow.url} element={<ProtectedRoute> <TransactionsView/></ProtectedRoute>}/>
                          <Route path={ROUTES.waitlists.url} element={<ProtectedRoute> <WaitlistView /> </ProtectedRoute>}/>
                          <Route path={ROUTES.transaction.url} element={<ProtectedRoute> <Tx/> </ProtectedRoute>}/>
                          <Route path={ROUTES.transactions.url} element={<ProtectedRoute> <TransactionsView /> </ProtectedRoute>}/>
                          <Route path={ROUTES.orders.url} element={<ProtectedRoute> <OrdersView /> </ProtectedRoute>}/>
                          <Route path={ROUTES.notifications.url} element={<ProtectedRoute> <Notifications/> </ProtectedRoute>}/>
                          <Route path={ROUTES.profile.url} element={<ProtectedRoute> <PrivateProfile /> </ProtectedRoute>}/>
                          <Route path={ROUTES.account.url} element={<ProtectedRoute> <UserAccount /> </ProtectedRoute>}/>
                          <Route path={ROUTES.order.url} element={<ProtectedRoute> <OrderDetails/> </ProtectedRoute>}/>
                          <Route path={ROUTES.cart.url} element={<ProtectedRoute> <CartPage/> </ProtectedRoute>}/>
                          <Route path={ROUTES.toLend.url} element={<ProtectedRoute> <Lend/> </ProtectedRoute>}/>
                          <Route path={ROUTES.lendTransaction.url} element={<ProtectedRoute> <TxDetail/> </ProtectedRoute>}/>
                          <Route path={ROUTES.lendTransactions.url} element={<ProtectedRoute> <LendTransactionsView/> </ProtectedRoute>}/>
                          <Route path='*' element={<ErrorPage/>} />
                      </Routes>
                  </Router>
              </BorrowProvider>
          </SessionProvider>
      </main>
      {/*Bottom bar with socials*/}
      <Footer/>
  </>
}

export default App
