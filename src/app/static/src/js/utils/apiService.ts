import {ActionContext, Commit} from "vuex";
import axios, {AxiosError, AxiosResponse} from "axios";

declare let appUrl: string | undefined;

export interface ResponseWithType<E> {
    data: E
}

export interface API<S, T> {
    withError: (type: T) => API<S, T>
    withSuccess: (type: S) => API<S, T>
    ignoreSuccess: () => API<S, T>
    ignoreError: () => API<S, T>

    postAndReturn<E>(url: string, data: any, config: Record<string, unknown>): Promise<void | ResponseWithType<E>>

    get<E>(url: string, config : {}): Promise<void | ResponseWithType<E>>

    delete(url: string): Promise<void | true>
}
export class ApiService<S extends string, T extends string> implements API<S, T>  {

    private readonly _baseUrl = typeof appUrl !== "undefined" ? appUrl: "";
    private _ignoreErrors = false;
    private _ignoreSuccess = false;
    private _onError: ((failure: Response) => void) | null = null;
    private _onSuccess: ((success: Response) => void) | null = null;
    private readonly _commit: Commit;
    private readonly _headers: any;

    constructor(context: ActionContext<any, any>) {
        this._commit = context.commit;
        this._headers = {"Accept-Language": "en-GB"};
    }

    private buildFullUrl = (url: string) => {
        return this._baseUrl + url
    };

    withSuccess = (type) => {
        this._onSuccess = (data: any) => {
            const toCommit = {type: type, payload: data};
            this._commit(toCommit)
        }
        return this;
    }

    withError = (type: T) => {
        this._onError = (error: Response) => {
            const toCommit = {type: type, payload: error}
            this._commit(toCommit);
        }
        return this;
    }

    ignoreSuccess = () => {
        this._ignoreSuccess = true
        return this
    }

    ignoreError = () => {
        this._ignoreErrors = true
        return this
    }

    private handleError = (e: AxiosError) => {
        console.log(e.response && e.response.data || e);
        if (this._ignoreErrors) {
            return
        }

        const failure = e.response && e.response.data;
        this._onError(failure);
    };

    private handleAxiosResponse(promise: Promise<AxiosResponse>) {
        return promise.then((axiosResponse: AxiosResponse) => {
            const success = axiosResponse && axiosResponse.data;
            const data = success.data;
            if (this._onSuccess) {
                this._onSuccess(data);
            }
            return success;
        }).catch((e: AxiosError) => {
            return this.handleError(e)
        });
    }

    async postAndReturn<E>(url, data, config = {}): Promise<void | ResponseWithType<E>> {
        const request = axios.post(this.buildFullUrl(url), data, {
            ...config,
            withCredentials: true,
            headers: this._headers
        })
        return this.handleAxiosResponse(request)
    }

    async get<E>(url: string, config = {}): Promise<void | ResponseWithType<E>> {
        const request = axios.get(this.buildFullUrl(url), {
            ...config,
            withCredentials: true,
            headers: this._headers
        })
        return this.handleAxiosResponse(request)
    }

    async delete(url: string): Promise<void | true> {
        const request = axios.delete(this.buildFullUrl(url), {
            withCredentials: true,
            headers: this._headers
        })
        return this.handleAxiosResponse(request)
    }
}

export const api = <S extends string, T extends string>(context: ActionContext<any, any>) => new ApiService<S, T>(context)