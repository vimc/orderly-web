import axios from "axios";

// appUrl var should be set externally in the browser
const baseUrl = typeof appUrl !== "undefined" ? appUrl: "";

const buildFullUrl = (url) => {
    return baseUrl + url
};

export const api = {
    baseUrl: baseUrl,
    get: (url) => axios.get(buildFullUrl(url), {withCredentials: true}),
    post: (url, data, config = {}) => axios.post(buildFullUrl(url), data, {...config, withCredentials: true}),
    delete: (url) => axios.delete(buildFullUrl(url), {withCredentials: true}),
};

