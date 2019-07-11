import axios from "axios";

// appUrl var should be set externally in the browser
const baseUrl = typeof appUrl !== "undefined" ? appUrl: "";

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

export const userService = {

    removeUserGroup: (userGroup, reportName) => {
        const data = {
            name: "reports.read",
            action: "remove",
            scope_prefix: "report",
            scope_id: reportName
        };

        return api.post(`/user-groups/${encodeURIComponent(userGroup)}/actions/associate-permission/`, data)
    }
};
