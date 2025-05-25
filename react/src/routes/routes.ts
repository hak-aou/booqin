
export const ROUTES = {
    home: {
        label: 'Home',
        needAuth: false,
        url: '/',
    },
    signUp: {
        label: 'Sign Up',
        needAuth: false,
        url: '/signup',
    },
    login: {
        label: 'Login',
        needAuth: false,
        url: '/login',
    },
    logout: {
        label: 'Logout',
        needAuth: false,
        url: '/logout',
    },
    account: {
        label: 'Account',
        needAuth: true,
        url: '/account'
    },
    profile: {
        label: 'Profile',
        needAuth: true,
        url: '/profile'
    },
    publicProfile: {
        label: 'Profile',
        needAuth: false,
        url: '/profile/:userId',
    },
    collectionDetail: {
        label: 'Collection Detail',
        needAuth: false,
        url: '/collections/:collectionId'
    },
    collections: {
        label: 'Collections',
        needAuth: false,
        url: '/collections'
    },
    borrow: {
        label: 'Borrow',
        needAuth: true,
        url: '/borrow/txs'
    },
    waitlists: {
        label: 'Borrow',
        needAuth: true,
        url: '/borrow/waitlist'
    },
    order: {
        label: 'Borrow Transaction',
        needAuth: true,
        url: '/cart/orders/:orderId'
    },
    orders: {
        label: 'Borrow Transaction',
        needAuth: true,
        url: '/cart/orders'
    },
    transactions: {
        label: 'Transactions',
        needAuth: true,
        url: '/borrow/txs'
    },
    transaction: {
        label: 'Transactions',
        needAuth: true,
        url: '/borrow/orders/:orderId/txs/:txId'
    },
    notifications: {
        label: 'Notifications',
        needAuth: true,
        url: '/notifications'
    },
    toLend: {
        label: 'Lend',
        needAuth: true,
        url: '/lend/to-lend'
    },
    lend: {
        label: 'Lend',
        needAuth: true,
        url: '/lend/txs'
    },
    lendTransactions: {
        label: 'Lend',
        needAuth: true,
        url: '/lend/txs'
    },
    lendTransaction: {
        label: 'Lend Transaction',
        needAuth: true,
        url: '/lend/txs/:txId'
    },
    cart: {
        label: 'Cart',
        needAuth: true,
        url: '/cart'
    },
    payment: {
            label: 'Payment',
            needAuth: true,
            url: '/payment/:cartId'
        },
    paymentSuccess: {
        label: 'Payment Success',
        needAuth: true,
        url: '/payment-success'
    },
    books : {
        label: 'Books',
        needAuth: false,
        url: '/book/:bookId'
    },
    search : {
        label: 'Search',
        needAuth: false,
        url: '/search/:searchData'
    },
    notificationPublisher: {
        label: 'Notification Publisher',
        needAuth: true,
        url: '/admin/notification-publisher'
    },
    admin: {
        label: 'Administration',
        needAuth: true,
        url: '/admin'
    }
}

export const needsAuth = (url: string) => {
    return Object.values(ROUTES).find(r => url.startsWith(r.url))?.needAuth || false;
}