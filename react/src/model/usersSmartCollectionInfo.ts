import {FilterBooksDTO} from "./filter.ts";
import {UserCollectionInfo} from "./userCollectionInfo.ts";

export interface UserSmartCollectionInfo extends UserCollectionInfo {
    filterBooksDTO?: FilterBooksDTO;
}

export interface SmartCollectionCreation {
    title: string;
    description: string;
    visibility: boolean;
    filterBooksDTO: FilterBooksDTO;
}
