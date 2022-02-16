<template>
    <div class="mb-3 mt-2 add-permission">
        <typeahead
                v-model="newPermission"
                size="sm"
                placeholder="name"
                :data="availablePermissions">
            <template slot="append">
                <button type="submit" class="btn btn-sm" @click="add">
                    Add
                </button>
            </template>
        </typeahead>
        <error-info :default-message="defaultMessage" :api-error="error" />
    </div>
</template>

<script>
    import Typeahead from "../typeahead/typeahead.vue";
    import ErrorInfo from "../errorInfo.vue";

    export default {
        name: 'AddPermission',
        components: {
            ErrorInfo,
            Typeahead
        },
        props: ['userGroup', 'availablePermissions'],
        data() {
            return {
                newPermission: "",
                error: "",
                defaultMessage: ""
            }
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
