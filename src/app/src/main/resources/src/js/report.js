import Vue from 'vue';
import axios from "axios";

let data = {error: ""};

// report var should be set externally in the browser
if (typeof report !== "undefined") {
    data = {...data, ...report};
}

export const vm = new Vue({
    el: '#vueApp',
    data: data,
    methods: {
        publish: () => {
            axios.post(`/v1/reports/${data.name}/versions/${data.id}/publish/`)
                .then(() => {
                    data.published = !data.published;
                })
                .catch(() => {
                    data.error = "An error occurred while publishing.";
                });
        }
    }
});
