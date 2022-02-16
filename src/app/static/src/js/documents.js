import Vue from "vue";
import documentPage from "./components/documents/documentPage.vue";

let canManage;

new Vue({
    el: '#app',
    data: {
        canManage
    },
    components: {
        documentPage
    }
});
