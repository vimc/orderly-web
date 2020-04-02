<template>
    <div>
        <p>Enter a Dropbox share link to download files from</p>
        <div class="input-group mb-3">
            <input type="text" class="form-control" placeholder="url" v-model="url" aria-label="url">
            <div class="input-group-append">
                <button class="btn btn-success" type="button" v-on:click="update">Update</button>
            </div>
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
        <div class="text-success small" v-if="hasSuccess">
            Documents have been updated!
        </div>
    </div>
</template>
<script>
    import {api} from "../../utils/api";
    import errorInfo from "../errorInfo";

    export default {
        name: "refreshDocuments",
        components: {errorInfo},
        data() {
            return {
                hasSuccess: false,
                defaultMessage: "Could not update documents",
                url: null,
                error: null
            }
        },
        methods: {
            update() {
                api.post("/documents/refresh", {url: this.url})
                    .then(() => {
                        this.hasSuccess = true;
                    })
                    .catch((error) => {
                        this.error = error;
                    })
            }
        }
    }
</script>