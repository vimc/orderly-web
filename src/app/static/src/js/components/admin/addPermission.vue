<template>
    <div class="mb-3 add-permission">
        <vue-bootstrap-typeahead
                size="sm"
                v-model="newPermission"
                placeholder="name"
                :data="availablePermissions">
            <template slot="append">
                <button v-on:click="add" type="submit" class="btn btn-sm">Add</button>
            </template>
        </vue-bootstrap-typeahead>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import VueBootstrapTypeahead from 'vue-bootstrap-typeahead'
    import ErrorInfo from "../errorInfo.vue";

    export default {
        name: 'addPermission',
        props: ['userGroup', 'availablePermissions'],
        data() {
            return {
                newPermission: "",
                error: "",
                defaultMessage: ""
            }
        },
        components: {
            ErrorInfo,
            VueBootstrapTypeahead
        },
        watch: {
            newPermission() {
                this.error = ""
            }
        },
        methods: {
            add: function () {
                if (!new Set(this.availablePermissions).has(this.newPermission)) {
                    const msg = `${this.newPermission} is not an available permission or already belongs to ${this.userGroup}`;
                    this.error = msg;
                    this.defaultMessage = msg;
                    return;
                }
                this.$emit("added", this.newPermission);
            }
        }
    };
</script>
