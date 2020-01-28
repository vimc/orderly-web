<template>
    <ul class="list-unstyled">
        <li v-for="p in permissions">
            {{p.name}} <span v-if="p.scope"> / {{p.scope}}</span>
        </li>
    </ul>
</template>

<script>
    export default {
        name: "permissionList",
        props: ["permissions"]
    }
</script>
