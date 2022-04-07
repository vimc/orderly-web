<template>
    <div class="mb-3">
        <label>Download files from:</label>
        <div class="input-group input-group-sm">
            <input v-model="url" type="text" class="form-control" placeholder="url"
                   aria-label="url">
            <div class="input-group-append">
                <button :disabled="!url || disabled" class="btn btn-success" type="button" @click="update">
                    {{ btnText }}
                </button>
            </div>
        </div>
        <div class="small text-muted mb-2">
            Provide a link to the zipped document folder, e.g. Dropbox share link
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
        <div v-if="hasSuccess" class="text-success small">
            Documents have been updated!
        </div>
    </div>
</template>
<script>
    import {api} from "../../utils/api";
    import errorInfo from "../errorInfo";

    export default {
        name: "RefreshDocuments",
        components: {errorInfo},
        data() {
            return {
                hasSuccess: false,
                defaultMessage: "could not update documents",
                url: null,
                error: null,
                disabled: false,
                btnText: "Update"
            }
        },
        methods: {
            update() {
                if (this.url) {
                    this.error = null;
                    this.hasSuccess = false;
                    this.disabled = true;
                    this.btnText = "...";
                    api.post("/documents/refresh", {url: this.url})
                        .then(() => {
                            this.disabled = false;
                            this.hasSuccess = true;
                            this.btnText = "Update";
                            this.$emit("refreshed");
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
