<template>
    <div>
        <div class="form-check">
            <input type="checkbox" class="form-check-input" id="allowGuest" v-model="authAllowGuest" @change="setAuthAllowGuest">
            <label class="form-check-label" for="allowGuest">Allow non-logged in users</label>
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import errorInfo from "../errorInfo.vue";
    import {api} from "../../utils/api";

    export default {
        name: "settings",
        components: {
            errorInfo: errorInfo
        },
        data() {
            return {
                url: "/settings/auth-allow-guest/",
                authAllowGuest: false,
                error: null,
                defaultMessage: ""
            }
        },
        methods: {
            setAuthAllowGuest: function() {
                api.post(this.url, this.authAllowGuest, {headers: {"Content-Type": "application/json"}})
                    .then(() => {
                        this.error = null;
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "could not set allow guest user";
                    });
            }
        },
        mounted() {
            api.get(this.url)
                .then(({data}) => {
                    this.error = null;
                    this.defaultMessage = "";

                    this.authAllowGuest = data.data
                })
                .catch((error) => {
                    this.error = error;
                    this.defaultMessage = "could not get allow guest user";
                });
        }
    }
</script>