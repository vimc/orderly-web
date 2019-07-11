<template>
    <div>
        <span class="reader-display-name">{{displayName}}</span>
        <span v-if="canRemove"
              v-on:click="$emit('remove', email)"
              class="remove-reader d-inline-block ml-2 large">×</span>
        <div class="text-muted small email">{{email}}</div>
    </div>
</template>

<script>
    export default {
        name: "reportReader",
        props: ["displayName", "email", "canRemove"]
    }
</script>