import {PageRequest} from "./common.ts";

export interface IntervalNumber {
    min: number;
    max: number;
}

export interface IntervalDate {
    min: Date | undefined;
    max: Date | undefined;
}

export interface FilterBooksDTO {
    title: string | undefined;
    hasSubtitle: boolean | undefined;
    categories: string[];
    languages: string[];
    authors: string[];
    pageCountInterval: IntervalNumber | undefined;
    publishedDateInterval: IntervalDate | undefined;
}

export interface FilterCollectionDTO {
    bookCountInterval: IntervalNumber;
    creationDateInterval: IntervalDate;
}

export interface FilterUserDTO {
    creationDateInterval: IntervalDate;
}

export interface FilterBooksRequest {
    filterBooksDTO: FilterBooksDTO;
    pageRequest: PageRequest;
}