interface UserPublicInfo {
    id: string;
    followableId: string;
    username: string;
    creationDate: any;
    imageUrl: string;
    numberOfFollowers: number;
}

interface UserPrivateInfo extends UserPublicInfo {
    email: string;
    isAdmin: boolean;
}

interface SignUpForm {
    username: string;
    email: string;
    phrase: string[];
}

export type {
    UserPublicInfo,
    UserPrivateInfo,
    SignUpForm
}