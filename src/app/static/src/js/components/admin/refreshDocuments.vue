<template>
    <div>
        <p>Enter a Dropbox share link to download files from</p>
        <div class="input-group input-group-sm mb-3">
            <input type="text" class="form-control" placeholder="url" v-model="url"
                   aria-label="url">
            <div class="input-group-append">
                <button :disabled="!url || disabled" class="btn btn-success" type="button" v-on:click="update">{{btnText}}</button>
            </div>
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
        <div class="text-success small" v-if="hasSuccess">
            Documents have been updated! <a :href="projectDocsHref">View</a>
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
                defaultMessage: "could not update documents",
                url: null,
                error: null,
                disabled: false,
                btnText: "Update",
                projectDocsHref: appUrl + "/project-docs"
            }
        },
        methods: {
            update() {
                if (this.url) {
                    this.error = null;
                    this.disabled = true;
                    this.btnText = "...";
                    api.post("/documents/refresh", {url: this.url})
                        .then(() => {
                            this.disabled = false;
                            this.hasSuccess = true;
                            this.btnText = "Update";
                        })
                        .catch((error) => {
                            this.disabled = false;
                            this.error = error;
                            this.btnText = "Update";
                        })
                }
            }
        }
    }
</script>