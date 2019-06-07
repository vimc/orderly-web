import axios from "axios";

export const api = {
    get: (url) => axios.get(url, {withCredentials: true}),
    post: (url, data) => axios.post(url, data, {withCredentials: true}),
    errorMessage: (response) => response &&
            response.data  &&
            response.data.errors &&
            response.data.errors[0] &&
            response.data.errors[0].message
};
