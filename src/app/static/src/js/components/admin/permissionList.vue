<template>
    <div>
    <ul class="list-unstyled children mt-1">
        <li v-for="p in permissions">
            <span class="name">{{p.name}} <span v-if="p.scope_prefix">/ {{p.scope_prefix}}:{{p.scope_id}}</span></span>
            <span v-on:click="function() {remove(p)}"
                  class="remove d-inline-block ml-2 large">×</span>
        </li>
    </ul>
    </div>
</template>

<script>
    import ErrorInfo from "../errorInfo";

    export default {
        name: "permissionList",
        components: {ErrorInfo},
        props: ["permissions", "email"],
        methods: {
            remove(p) {
                this.$emit("removed", p)
            }
        }
    }
</script>
