
export interface PageRequest {
    offset: number;
    limit: number;
}

export interface PaginatedResult<T> {
    data: T[];
    totalResults : number,
    numberInPage: number,
    offset: number;
    limit: number;
}

// immutable data scroller
export class DataPageScroller<T> {
    readonly data: T[];
    readonly numberResultMax: number;
    readonly offset: number;
    readonly limit: number;
    private launchedOnce: boolean = false;
    private readonly getMoreData: (page: PageRequest) => Promise<PaginatedResult<T>>;

    constructor(data: T[], numberResultMax: number, offset: number, limit: number, fetchFunction: (page: PageRequest) => Promise<PaginatedResult<T>>) {
        this.data = data;
        this.numberResultMax = numberResultMax;
        this.offset = offset;
        this.limit = limit;
        this.getMoreData = fetchFunction;
    }

    async fetchMoreData(): Promise<DataPageScroller<T>> {
        if(this.launchedOnce && this.offset >= this.numberResultMax) {
            return new DataPageScroller<T>(this.data, this.numberResultMax, this.offset, this.limit, this.getMoreData);
        }
        const result = await this.getMoreData({offset: this.offset, limit: this.limit});
        if(result.data.length === 0) {
            this.launchedOnce = false;
        }
        console.log("aaaa " , result.data.length);
        const scroller = new DataPageScroller<T>(
            [...this.data, ...result.data],
            result.totalResults,
            this.offset + result.data.length,
            this.limit,
            this.getMoreData
        );
        console.log("bbb ", this.offset)
        console.log(scroller);
        scroller.launchedOnce = true;
        return scroller;
    }

    empty(): boolean {
        return this.data.length === 0;
    }

    withNewData(data: T[]): DataPageScroller<T> {
        return new DataPageScroller<T>(data,
            this.numberResultMax,
            this.offset,
            this.limit,
            this.getMoreData);
    }

    canFetchMore(): boolean {
        return this.remainingToFetch() > 0
    }

    remainingToFetch(): number {
        return this.numberResultMax - this.data.length;
    }
}