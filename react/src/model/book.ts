interface IsbnModel {
    isbn_13: string;
    isbn_10: string;
}

interface ImageFormatModel {
    small: string;
    medium: string;
    large: string;
}

interface Book {
    id: string;
    votableId: string;
    commentableId: string;
    followableId: string;
    title: string;
    isbn: IsbnModel | null;
    authors: string[] | null;
    publishers: string[];
    publishedDate: string | null;
    categories: string[];
    language: string;
    imageLinks: ImageFormatModel | null;
    subtitle: string | null;
    description: string | null;
    pageCount: number | null;
}

export interface BookInfo {
    id: string,
    commentableId: string;
    isbn: IsbnModel;
    title: string;
    subtitle: string;
    categories: string[];
    imageLinks: ImageFormatModel;
    supply: number;
}

export interface BookAvailability {
    bookId: string;
    supply: number;
    demand: number;
    isBorrowed: boolean;
    isLent: boolean;
    isInCart: boolean;
    inTx: InTx | null;
}

export interface InTx {
    orderId: string | null;
    txId: string;
}

export interface WaitListStatus {
    bookId: string;
    title: string;
    images: ImageFormatModel;
    isLocked : boolean;
    lockedUntil: any;
}

export interface LoanOffer {
    bookId: string;
    title: string;
    images: ImageFormatModel;
    quantity: number;
}

export interface LendRequest {
    bookId: string;
    quantity: number;
}


export default Book;