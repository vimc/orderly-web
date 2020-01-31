<template>
    <div class="mb-3 mt-2 add-permission">
        <typeahead
                size="sm"
                v-model="newPermission"
                placeholder="name"
                :data="availablePermissions">
            <template slot="append">
                <button v-on:click="add" type="submit" class="btn btn-sm">Add</button>
            </template>
        </typeahead>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import Typeahead from "../typeahead/typeahead.vue";
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
            Typeahead
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
