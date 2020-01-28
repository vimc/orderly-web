<template>
    <ul class="list-unstyled">
        <li v-for="p in permissions">
            
        </li>
    </ul>
</template>

<script>
    export default {
        name: "permissionList",
        props: ["permissions"]
    }
</script>
