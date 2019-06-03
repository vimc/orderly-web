import axios from "axios";

export const api = {
    get: (url) => axios.get(url, {withCredentials: true}),
    post: (url, data) => axios.post(url, data, {withCredentials: true})
};
