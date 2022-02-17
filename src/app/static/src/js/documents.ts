import Vue from "vue";
import documentPage from "./components/documents/documentPage.vue";

declare const canManage: boolean;

new Vue({
    el: '#app',
    components: {
        documentPage
    },
    data: {
        canManage
    }
});
