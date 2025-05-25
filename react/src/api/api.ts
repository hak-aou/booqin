import {SessionMethods} from "../hooks/session/sessionContext.tsx";
import {booqinRequest} from "./authentication.tsx";
import {AuthIdentity} from "../model/session.ts";
import {FollowersRequest, FollowRelationship} from "../model/followers.ts";
import {AxiosResponse} from "axios";
import {NotificationToken, UserNotifications} from "../model/notification.ts";
import {PageRequest, PaginatedResult} from "../model/common.ts";
import {Comment, CommentCommentable, CommentData, ReplyComment} from "../model/comment.ts";
import {SignUpForm, UserPrivateInfo, UserPublicInfo} from "../model/userPublicInfo.ts";
import {API_ENDPOINTS} from "./endpoints.ts";
import {HasVoteDTO} from "../model/vote.ts";
import {FilterBooksDTO} from "../model/filter.ts";
import {BookLendTransaction, Cart, ChargeRequest, Checkout, Order} from "../model/Cart.ts";
import {BookAvailability, LoanOffer, LendRequest, WaitListStatus, BookInfo} from "../model/book.ts";
import {
    CollectionCreation,
    UserCollectionInfo,
} from "../model/userCollectionInfo.ts";
import {SmartCollectionCreation, UserSmartCollectionInfo} from "../model/usersSmartCollectionInfo.ts";




export interface Api {
    getPublicProfile: (userId: string) => Promise<UserPublicInfo>
    notificationToken: () => Promise<NotificationToken>

    // Fetch
    fetchAndUpdateMyUserAccountInfo: () => void
    fetchAndUpdateMyCollections: () => void;
    fetchAndUpdateMyNotifications: (pageNumber: number, pageSize: number) => Promise<any>

    // Follow
    follow: (followableId: string) => Promise<AxiosResponse<any, any>>
    unfollow: (followableId: string) => Promise<AxiosResponse<any, any>>
    getFollowers: (userId: string, page: PageRequest) => Promise<PaginatedResult<UserPublicInfo>>
    getRelationship: (followableId: string) => Promise<FollowRelationship>
    followUser: (followableId: string) => Promise<AxiosResponse<any, any>>
    getUsersFollowedByAGivenUser: (userId: string, page: PageRequest) => Promise<PaginatedResult<UserPublicInfo>>

    // notification
    getNotifications: (page: PageRequest) => Promise<UserNotifications>

    // Comment
    getComments: (commentableId: string, page: PageRequest) => Promise<PaginatedResult<Comment>>
    getReplies: (commentId: number, page: PageRequest) => Promise<PaginatedResult<Comment>>
    replyToComment: (parentId: number, content: string) => Promise<CommentData>
    commentCommentable: (commentableId: string, content: string) => Promise<CommentData>
    editComment: (commentData: CommentData) => Promise<void>
    obfuscateComment: (commentId: number) => Promise<void>
    deleteComment: (commentId: number) => Promise<void>

    // Vote
    upvote: (votableId: string) => Promise<AxiosResponse<any, any>>
    downvote: (votableId: string) => Promise<AxiosResponse<any, any>>
    unvote: (votableId: string) => Promise<AxiosResponse<any, any>>
    votevalue: (votableId: string) => Promise<number>
    hasvoted: (votableId: string) => Promise<HasVoteDTO>

    // Collections
    getUserCollection: () => Promise<Set<UserCollectionInfo>>;
    getCollection: (collectionId: number) => Promise<UserCollectionInfo>;
    getAllPublicCollection: (page: PageRequest) => Promise<PaginatedResult<UserCollectionInfo>>;
    getBooksOfCollection: (collectionId: number, page: PageRequest) => Promise<PaginatedResult<BookInfo>>;
    createCollection: (collection: CollectionCreation) => Promise<UserCollectionInfo>;

    // smart collection
    createSmartCollection: (smartCollection: SmartCollectionCreation) => Promise<UserSmartCollectionInfo>;
    getSmartCollection: (collectionId: number) => Promise<UserSmartCollectionInfo>;
    getSmartCollectionFilter: (collectionId: number) => Promise<FilterBooksDTO>;

    // search
    searchCollections: (searchData: string, page: PageRequest) => Promise<PaginatedResult<UserCollectionInfo>>;
    searchBooks: (searchData: string, page: PageRequest) => Promise<PaginatedResult<BookInfo>>;
    searchUsers: (searchData: string, page: PageRequest) => Promise<PaginatedResult<UserPublicInfo>>;

    // filter / smart collection
    filterBooks:(collectionId: number, filterBooksDTO : FilterBooksDTO, page: PageRequest) => Promise<PaginatedResult<BookInfo>>;
    languages: () => Promise<string[]>;
    categories: () => Promise<string[]>;

    // book
    whichCollection: (bookId: string) => Promise<UserCollectionInfo[]>;
    addBookToCollection: (collectionId: number, bookId: string) => Promise<void>;
    removeBookFromCollection: (collectionId: number, id: string) => Promise<void>;
    getBookAvailability: (bookId: string) => Promise<BookAvailability>;

    // cart & checkout
    borrowBook: (bookId: string) => Promise<void>;
    unBorrowBook: (bookId: string) => Promise<void>;
    unLendBook: (bookId: string) => Promise<void>;
    getWaitLists: () => Promise<WaitListStatus[]>;
    getLoanOffers: () => Promise<LoanOffer[]>;
    lendBook: (request: LendRequest) => Promise<void>;
    getCart: () => Promise<Cart>;
    waitlistAccept: (bookId: string) => Promise<void>;
    removeBookFromCart: (bookId: string) => Promise<void>;
    charge: (param: ChargeRequest ) => Promise<any>;
    completePayment: (paymentIntentId: any) => Promise<any>;
    checkout: (cartVersion: number) => Promise<Checkout>;
    myBorrowTransactions: () => Promise<Order[]>;
    myLendTransactions: () => Promise<BookLendTransaction[]>;
    processTransactionNextStep: (txId: string) => Promise<void>;
    deleteNotifications: (strings: string[]) => Promise<void>;
    getShippingLabel: (txId: string) => Promise<Blob>;
    signUp: (param: SignUpForm) => any;
    sendNotificationToAll: (notificationMessage: string) => void;
    updateCollection: (collectionId: number, collectionUpdateForm: CollectionCreation) => Promise<void>;
    deleteCollection: (id: number) => Promise<any>;
}

export const apiWithGivenSession = (sessionMethods: SessionMethods): Api => {
    return {
        // User
        fetchAndUpdateMyUserAccountInfo: () => {
            booqinRequest(sessionMethods).get(API_ENDPOINTS.me)
                .then((response) => {
                    const user: UserPrivateInfo = response.data
                    sessionMethods.setAccountInfo(user)
                });
        },
        getPublicProfile: async (userId: string) => {
            const response = await booqinRequest(sessionMethods)
                .get<UserPublicInfo>(API_ENDPOINTS.user.replace(':userId', userId));
            return response.data as UserPublicInfo;
        },
        getFollowers: async (objectId: string, page: PageRequest = {offset: 0, limit: 10}) => {
            const followersRequest: FollowersRequest = {
                objectId: objectId,
                pageRequest: page
            }
            const response = await booqinRequest(sessionMethods)
                .post<FollowersRequest, any>(API_ENDPOINTS.followers, followersRequest);
            return response.data as PaginatedResult<UserPublicInfo>;
        },
        getRelationship: async (userId: string) => {
            const response = await booqinRequest(sessionMethods)
                .post<FollowRelationship>(API_ENDPOINTS.followRelationship.replace(':followableId', userId));
            return response.data as FollowRelationship;
        },
        follow: (followableId: string) => {
            return booqinRequest(sessionMethods)
                .post(API_ENDPOINTS.follow.replace(':followableId', followableId));
        },
        followUser: (followableId: string) => {
            return booqinRequest(sessionMethods)
                .post(API_ENDPOINTS.followUser.replace(':followableId', followableId));
        },
        unfollow: (followableId: string) => {
            return booqinRequest(sessionMethods)
                .delete(API_ENDPOINTS.unfollow.replace(':followableId', followableId));
        },

        // Vote
        upvote: (votableId: string) => {
            return booqinRequest(sessionMethods)
                .post(API_ENDPOINTS.upvote.replace(':votableId', votableId));
        },
        downvote: (votableId: string) => {
            return booqinRequest(sessionMethods)
                .post(API_ENDPOINTS.downvote.replace(':votableId', votableId));
        },
        unvote: (votableId: string) => {
            return booqinRequest(sessionMethods)
                .post(API_ENDPOINTS.unvote.replace(':votableId', votableId));
        },
        votevalue: async (votableId: string) => {
            const response = await booqinRequest(sessionMethods)
                .get<number>(API_ENDPOINTS.votevalue.replace(':votableId', votableId));
            return response.data;
        },
        hasvoted: async (votableId: string) => {
            const response = await booqinRequest(sessionMethods)
                .post<HasVoteDTO>(API_ENDPOINTS.hasvoted.replace(':votableId', votableId));
            return response.data as HasVoteDTO;
        },

        notificationToken: async () => {
            const response = await booqinRequest(sessionMethods)
                .post<NotificationToken>(API_ENDPOINTS.notificationToken);
            return response.data as NotificationToken;
        },
        fetchAndUpdateMyNotifications: async (pageNumber: number, pageSize: number) => {
            const pageRequest: PageRequest = {
                offset: pageNumber,
                limit: pageSize
            }
            const response = await booqinRequest(sessionMethods)
                .post<any>(`${API_ENDPOINTS.notifications}`, pageRequest);
            return response.data as UserNotifications;
        },
        getNotifications: async (page: PageRequest = {offset: 0, limit: 10}) => {
            const response = await booqinRequest(sessionMethods)
                .post<UserNotifications>(API_ENDPOINTS.notifications, page);
            return response.data as UserNotifications;
        },
        deleteNotifications: async (notificationIds: string[]) => {
            const response = await booqinRequest(sessionMethods)
                .delete(API_ENDPOINTS.notifications, { data: {notificationId: notificationIds} });
            return response.data;
        },
        getUsersFollowedByAGivenUser: async (userId: string, page: PageRequest = {offset: 0, limit: 10}) => {
            const followingsRequest = {
                userId: userId,
                pageRequest: page
            } as any;
            const response = await booqinRequest(sessionMethods)
                .post<FollowersRequest, any>(API_ENDPOINTS.followings, followingsRequest);
            return response.data as PaginatedResult<UserPublicInfo>;
        },
        getComments: async (commentableId: string, page: PageRequest = {offset: 0, limit: 10}) => {
            const response = await booqinRequest(sessionMethods)
                .post<PaginatedResult<Comment>>(API_ENDPOINTS.comments, {objectId: commentableId, pageRequest: page});
            return response.data
        },
        replyToComment: async (parentId: number, content: string) => {
            const comment = {
                parentId: parentId,
                content: content,
            } as ReplyComment;
            const response = await booqinRequest(sessionMethods)
                .post(API_ENDPOINTS.replyToComment, comment);
            return response.data as CommentData;
        },
        getReplies: async (commentId: number, page: PageRequest = {offset: 0, limit: 10}) => {
            const response = await booqinRequest(sessionMethods)
                .post<PaginatedResult<Comment>>(API_ENDPOINTS.replies.replace(':commentId', commentId.toString()), page);
            return response.data as PaginatedResult<Comment>;
        },
        editComment: async (commentData: CommentData) => {
            const response = await booqinRequest(sessionMethods)
                .patch(API_ENDPOINTS.comment, commentData);
            return response.data;
        },
        commentCommentable: async (commentableId: string, content: string) => {
            const comment = {
                commentableId: commentableId,
                content: content,
            } as CommentCommentable;
            const response = await booqinRequest(sessionMethods)
                .post(API_ENDPOINTS.comment, comment);
            return response.data as CommentData;
        },
        obfuscateComment: async (commentId: number) => {
            const response = await booqinRequest(sessionMethods)
                .post(API_ENDPOINTS.obfuscateComment.replace(':commentId', commentId.toString()));
            return response.data;
        },
        deleteComment: async (commentId: number) => {
            const response = await booqinRequest(sessionMethods)
                .delete(API_ENDPOINTS.deleteComment.replace(':commentId', commentId.toString()));
            return response.data;
        },
        // Collections
        getUserCollection: async () => {
            const response = await booqinRequest(sessionMethods).get("/collection/getAll");
            return response.data as Promise<Set<UserCollectionInfo>>
        },
        getAllPublicCollection: async (page: PageRequest): Promise<PaginatedResult<UserCollectionInfo>> => {
            const response = await booqinRequest(sessionMethods)
                .post( "/collection/getAllPublic", page);
            return response.data as Promise<PaginatedResult<UserCollectionInfo>>
        },
        getCollection: async (collectionId: number) => {
            const response = await booqinRequest(sessionMethods).get(`/collection/${collectionId}`);
            return response.data as Promise<UserCollectionInfo>;
        },
        fetchAndUpdateMyCollections: async () => {
            booqinRequest(sessionMethods).get(API_ENDPOINTS.myCollections)
                .then((response) => {
                    console.log(response.data)
                    const collections: UserCollectionInfo[] = response.data
                    sessionMethods.setCollections(collections)
                });
        },
        getBooksOfCollection: async (collectionId: number, page: PageRequest) => {
            const response = await booqinRequest(sessionMethods).post(
                API_ENDPOINTS.booksOfCollection.replace(':collectionId', collectionId.toString()), page);
            return response.data as PaginatedResult<BookInfo>;
        },
        searchCollections: async (searchData: string, page: PageRequest) => {
            const response = await booqinRequest(sessionMethods).post(
                API_ENDPOINTS.searchCollections.replace(':searchData', searchData), page);
            return response.data as PaginatedResult<UserCollectionInfo>;
        },
        searchBooks: async (searchData: string, page: PageRequest) => {
            const response = await booqinRequest(sessionMethods).post(
                API_ENDPOINTS.searchBooks.replace(':searchData', searchData), page);
            return response.data as PaginatedResult<BookInfo>;
        },
        searchUsers: async (searchData: string, page: PageRequest) => {
            const response = await booqinRequest(sessionMethods).post(
                API_ENDPOINTS.searchUsers.replace(':searchData', searchData), page);
            return response.data as PaginatedResult<UserPublicInfo>;
        },
        filterBooks: async (collectionId: number, filterBooksDTO: FilterBooksDTO, page: PageRequest) => {
            const filterBookRequest = {
                filterBooksDTO: filterBooksDTO,
                pageRequest: page
            }

            const response = await booqinRequest(sessionMethods).post(
                API_ENDPOINTS.filterBooks.replace(':collectionId', collectionId.toString()), filterBookRequest);
            return response.data as PaginatedResult<BookInfo>;
        },
        languages: async () => {
            const response = await booqinRequest(sessionMethods).get(API_ENDPOINTS.languages);
            return response.data as string[];
        },
        categories: async () => {
            const response = await booqinRequest(sessionMethods).get(API_ENDPOINTS.categories);
            return response.data as string[];
        },
        createCollection: async (collection: CollectionCreation) => {
            const response = await booqinRequest(sessionMethods).post(API_ENDPOINTS.collection, collection);
            return response.data as UserCollectionInfo;
        },
        createSmartCollection: async (smartCollection: SmartCollectionCreation) => {
            const response = await booqinRequest(sessionMethods).post(API_ENDPOINTS.smartCollection, smartCollection);
            return response.data as UserSmartCollectionInfo;
        },
        getSmartCollection: async (collectionId: number) => {
            const response = await booqinRequest(sessionMethods).get(API_ENDPOINTS.smartCollection + "/" + collectionId.toString());
            return response.data as UserSmartCollectionInfo;
        },
        getSmartCollectionFilter: async (collectionId: number) => {
            const response = await booqinRequest(sessionMethods).get(API_ENDPOINTS.getSmartCollectionFilter.replace(':collectionId', collectionId.toString()));
            return response.data as FilterBooksDTO;
        },
        whichCollection: async (bookId: string) => {
            const response = await booqinRequest(sessionMethods).get(API_ENDPOINTS.collectionContains.replace(':bookId', bookId));
            return response.data as UserCollectionInfo[];
        },
        addBookToCollection: async (collectionId: number, bookId: string) => {
            const response = await booqinRequest(sessionMethods).post(
                API_ENDPOINTS.bookToCollection.replace(':collectionId', collectionId.toString())
                    .replace(':bookId', bookId));
            return response.data;
        },
        removeBookFromCollection: async (collectionId: number, id: string) => {
            const response = await booqinRequest(sessionMethods).delete(
                API_ENDPOINTS.bookToCollection.replace(':collectionId', collectionId.toString())
                    .replace(':bookId', id));
            return response.data;
        },
        getBookAvailability: async (bookId: string) => {
            const response = await booqinRequest(sessionMethods).get(
                API_ENDPOINTS.bookAvailability.replace(':bookId', bookId.toString()));
            return response.data as BookAvailability;
        },
        borrowBook: async (bookId: string) => {
            const response = await booqinRequest(sessionMethods).post(
                API_ENDPOINTS.borrowBook.replace(':bookId', bookId.toString()));
            return response.data;
        },
        unBorrowBook: async (bookId: string) => {
            const response = await booqinRequest(sessionMethods).delete(
                API_ENDPOINTS.updateLendBook.replace(':bookId', bookId.toString()));
            return response.data;
        },
        unLendBook: async (bookId: string) => {
            const response = await booqinRequest(sessionMethods).patch(
                API_ENDPOINTS.lendBook + "/" + bookId.toString())
            return response.data;
        },
        getWaitLists: async () => {
            const response = await booqinRequest(sessionMethods).get(API_ENDPOINTS.waitLists);
            return response.data as WaitListStatus[];
        },
        getLoanOffers: async () => {
            const response = await booqinRequest(sessionMethods).get(API_ENDPOINTS.loanOffers);
            return response.data as LoanOffer[];
        },
        lendBook: async (request: LendRequest) => {
            const response = await booqinRequest(sessionMethods).post(
                API_ENDPOINTS.lendBook, request);
            return response.data;
        },
        getCart: async () => {
            const response = await booqinRequest(sessionMethods).get(API_ENDPOINTS.cart);
            return response.data as Cart;
        },
        waitlistAccept: async (bookId: string) => {
            const response = await booqinRequest(sessionMethods).post(
                API_ENDPOINTS.waitlistAccept.replace(':bookId', bookId));
            return response.data;
        },
        removeBookFromCart: async (bookId: string) => {
            const response = await booqinRequest(sessionMethods).delete(
                API_ENDPOINTS.cartBook.replace(':bookId', bookId));
            return response.data;
        },
        charge: async (chargeRequest: ChargeRequest) => {
            const response = await booqinRequest(sessionMethods).post(
                API_ENDPOINTS.stripePayment, chargeRequest);
            return response.data;
        },
        completePayment: async (paymentIntentId: any) => {
            const response = await booqinRequest(sessionMethods).post(
                API_ENDPOINTS.completePayment.replace(':paymentIntentId', paymentIntentId));
            return response.data;
        },
        checkout: async (cartVersion: number) => {
            const response = await booqinRequest(sessionMethods).post(
                API_ENDPOINTS.cartCheckout.replace(':cartVersion', cartVersion.toString()));
            return response.data
        },
        myBorrowTransactions: async () => {
            const response = await booqinRequest(sessionMethods)
                .get(API_ENDPOINTS.borrowTransactions);
            return response.data as Order[];
        },
        myLendTransactions: async () => {
            const response = await booqinRequest(sessionMethods)
                .get(API_ENDPOINTS.lendTransactions);
            return response.data as BookLendTransaction[];
        },
        processTransactionNextStep: async (txId: string) => {
            const response = await booqinRequest(sessionMethods)
                .post(API_ENDPOINTS.processTransactionNextStep.replace(':txId', txId));
            return response.data;
        },
        getShippingLabel: async (txId: string) => {
            const response = await booqinRequest(sessionMethods)
                .get(API_ENDPOINTS.shippingLabel.replace(':txId', txId),
                    {
                        responseType: 'arraybuffer',
                        headers: {
                            'Accept': 'application/pdf'
                        }
                    }
                    );
            return new Blob([response.data], { type: 'application/pdf' });
        },
        signUp: async (signUpForm: SignUpForm) => {
            const response = await booqinRequest(sessionMethods)
                .post(API_ENDPOINTS.signUp, signUpForm);
            return response.data;
        },
        sendNotificationToAll: async (notificationMessage: string) => {
            const response = await booqinRequest(sessionMethods)
                .post(API_ENDPOINTS.notificationPublisher, {message: notificationMessage});
            return response.data
        },
        updateCollection: async (collectionId, collectionUpdateForm: CollectionCreation) => {
            const response = await booqinRequest(sessionMethods)
                .patch(API_ENDPOINTS.collection + `/${collectionId}`, collectionUpdateForm);
            return response.data;
        },
        deleteCollection: async (id: number) => {
            const response = await booqinRequest(sessionMethods)
                .delete(API_ENDPOINTS.collection + `/${id}`);
            return response.data;
        }
    }
}

/*
    Used as a placeholder when the session is not yet initialized.
 */
export const emptyApi = (): Api => {
    return new Proxy<Api>({} as Api, {
        get: (_target, prop) => {
            if (typeof prop === 'string') {
                return () => {
                    console.error(`Something went wrong. Api method ${prop} should be bound to a session.`);
                };
            }
        }
    });
};

/////// Authentication is a special case, it is not part of the session ///////

export function getAuthIdentity(sessionMethods: SessionMethods) {
    booqinRequest(sessionMethods).get<AuthIdentity>(API_ENDPOINTS.authMe)
        .then((response) => {
            sessionMethods.setAuthIdentity(response.data)
        });
}

export function logout(sessionMethods: SessionMethods) {
    console.log("Logout successful");
    booqinRequest(sessionMethods).post(API_ENDPOINTS.logout).then(() => {
        console.log("Logout successful")
    });
    sessionMethods.logout()
}
