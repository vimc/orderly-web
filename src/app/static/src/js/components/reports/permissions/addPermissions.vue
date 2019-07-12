<template>
    <div class="mb-3">
        <vue-bootstrap-typeahead
                size="sm"
                v-model="newItem"
                :placeholder="placeholder"
                :data="availableItems">
            <template slot="append">
                <button v-on:click="add" type="submit" class="btn btn-sm">{{addText}}</button>
            </template>
        </vue-bootstrap-typeahead>
        <error-info :error="error" :default-message="defaultMessage"></error-info>
    </div>
</template>

<script>
    import {api} from "../../../utils/api";
    import VueBootstrapTypeahead from 'vue-bootstrap-typeahead'
    import ErrorInfo from "../../errorInfo.vue";

    export default {
        name: 'addPermissions',
        components: {ErrorInfo, VueBootstrapTypeahead},
        props: ['report', 'availableItems', 'placeholder', 'addText'],
        data() {
            return {
                newItem: "",
                error: null,
                defaultMessage: ""
            }
        },
        watch: {
            newItem() {
                this.error = null
            }
        },
        methods: {
            add: function () {
                if (!new Set(this.availableItems).has(this.newItem)) {
                    this.error = `You must enter a valid ${this.placeholder}`;
                    this.defaultMessage
                        = `You must enter a valid ${this.placeholder}`;
                    return;
                }

                const data = {
                    name: "reports.read",
                    action: "add",
                    scope_prefix: "report",
                    scope_id: this.report.name
                };

                api.post(`/user-groups/${encodeURIComponent(this.newItem)}/actions/associate-permission/`, data)
                    .then(() => {
                        this.$emit('added');
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = `could not add ${this.newItem}`;
                    });
            }
        }
    };
</script>