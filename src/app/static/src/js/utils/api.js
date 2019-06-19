import axios from "axios";

// appUrl var should be set externally in the browser
const baseUrl = typeof appUrl !== "undefined" ? appUrl: "http://app";

const buildFullUrl = (url) => {
    return baseUrl + url
};

export const api = {
    baseUrl: baseUrl,
    get: (url) => axios.get(buildFullUrl(url), {withCredentials: true}),
    post: (url, data) => axios.post(buildFullUrl(url), data, {withCredentials: true}),
    errorMessage: (response) => response &&
            response.data  &&
            response.data.errors &&
            response.data.errors[0] &&
            response.data.errors[0].message
};

