<template>
    <div class="mb-3">
        <typeahead v-model="newUserGroup"
                   size="sm"
                   :placeholder="placeholder"
                   :data="availableUserGroups">
            <template slot="append">
                <button type="submit" class="btn btn-sm" @click="add">
                    Add {{ type }}
                </button>
            </template>
        </typeahead>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import Typeahead from "../typeahead/typeahead.vue";
    import ErrorInfo from "../errorInfo.vue";

    export default {
        name: 'AddReportReader',
        components: {
            ErrorInfo,
            Typeahead
        },
        props: ['reportName', 'availableUserGroups', 'type'],
        data() {
            return {
                newUserGroup: "",
                error: "",
                defaultMessage: ""
            }
        },
        computed: {
            placeholder: function () {
                return this.type === "user" ? "email" : "role name";
            }
        },
        watch: {
            newUserGroup() {
                this.error = ""
            }
        },
        methods: {
            add: function () {

                if (!new Set(this.availableUserGroups).has(this.newUserGroup)) {
                    this.error = `${this.newUserGroup} is not a valid ${this.placeholder} or already has this permission`;
                    this.defaultMessage = `${this.newUserGroup} is not a valid ${this.placeholder} or already has this permission`;
                    return;
                }

                const data = {
                    name: "reports.read",
                    scope_prefix: "report",
                    scope_id: this.reportName,
                    action: "add"
                };

                api.post(`/${this.type}s/${encodeURIComponent(this.newUserGroup)}/permissions/`, data)
                    .then(() => {
                        this.newUserGroup = "";
                        this.error = null;
                        this.$emit("added");
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = `could not add ${this.type}`;
                    });
            }
        }
    };
</script>