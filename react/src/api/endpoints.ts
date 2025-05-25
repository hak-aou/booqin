const API_ROOT = import.meta.env.VITE_API_URL;

export const API_ENDPOINTS = {
    root: API_ROOT,
    login: '/auth/login',
    logout: '/auth/logout',
    refresh: '/auth/refresh',
    authMe: '/auth/me',

    // user
    me: '/user/me',
    user: '/user/:userId',
    signUp: '/user',

    // follow
    followers: '/follow/followers',
    followRelationship: '/follow/relationship/:followableId',
    follow: '/follow/:followableId',
    followUser: '/follow/users/:followableId',
    unfollow: '/follow/:followableId',
    followings: "/follow/followings",

    // notification
    notifications: '/notifications',
    notificationToken: '/notifications/token',
    notificationsSubscribe: '/notifications/subscribe/:token',

    // comment
    comments: "/comment/all",
    replyToComment: "/comment/reply",
    comment: "/comment",
    replies: "/comment/replies/:commentId",
    obfuscateComment: "comment/obfuscate/:commentId",
    deleteComment: "/comment/:commentId",

    // vote
    upvote: 'vote/upvote/:votableId',
    downvote: 'vote/downvote/:votableId',

    unvote: 'vote/unvote/:votableId',
    votevalue: 'vote/votevalue/:votableId',
    hasvoted: 'vote/hasvoted/:votableId',

    // collection
    collection: '/collection',
    myCollections: '/collection/getAll',
    booksOfCollection: '/collection/:collectionId/books',
    collectionContains: '/collection/contains/:bookId',
    bookToCollection: '/collection/:collectionId/book/:bookId', // add or remove book from collection

    // smart collection
    smartCollection: '/smart_collection',
    getSmartCollections: '/smart_collection/:collectionId',
    getSmartCollectionFilter: '/smart_collection/filter/:collectionId',

    // book
    book: '/books/:bookId',
    bookAvailability: '/books/:bookId/availability',
    borrowBook: '/books/:bookId/borrow',
    lendBook: '/books/lend',
    updateLendBook: '/books/:bookId/lend',
    waitlistAccept: '/books/:bookId/waitlistAccept',
    waitLists: "/books/waitlists",
    loanOffers: "/books/loans",

    // search
    // search: '/search/:searchData',
    searchCollections: '/search/collections/:searchData',
    searchBooks: '/search/books/:searchData',
    searchUsers: '/search/users/:searchData',

    // filter / smart collection
    filterBooks:'/smart_collection/filter/:collectionId/books',
    languages: '/books/languages',
    categories: '/books/categories',

    // Cart
    cart: "/cart",
    cartBook: "/cart/:bookId",
    cartCheckout: "/cart/checkout/:cartVersion",
    stripePayment: "/cart/stripe/charge",
    completePayment: "/cart/stripe/complete/:paymentIntentId",

    // Tx
    borrowTransactions: '/txs/borrows',
    lendTransactions: '/txs/loans',
    processTransactionNextStep: '/txs/:txId/nextStep',
    shippingLabel: '/txs/:txId/shipping-label',

    // Admin
    notificationPublisher: '/admin/notification',

};


