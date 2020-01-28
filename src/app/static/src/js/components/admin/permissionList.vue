<template>
    <ul class="list-unstyled children mt-1">
        <li v-for="p in permissions">
            {{p.name}} <span v-if="p.scope_prefix"> / {{p.scope_prefix}}:{{p.scope_id}}</span>
            <span v-on:click="function() {remove(p)}"
                  class="remove-user-group d-inline-block ml-2 large">×</span>
        </li>
    </ul>
</template>

<script>
    import {api} from "../../utils/api";

    export default {
        name: "permissionList",
        props: ["permissions", "email"],
        methods: {
            remove(p) {
                const data = {
                    action: "remove",
                    ...p
                };

                api.post(`/user-groups/${encodeURIComponent(this.email)}/actions/associate-permission/`, data)
                    .then(() => {
                        this.error = null;
                        this.$emit("removed", p);
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = `could not remove ${p.name} from ${this.email}`;
                    });
            }
        }
    }
</script>
