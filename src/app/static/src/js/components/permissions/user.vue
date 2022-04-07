<template>
    <div class="removable-user">
        <span class="display-name">{{ displayName }}</span>
        <span v-if="canRemove" class="remove d-inline-block ml-2 large"
              @click="$emit('removed', email)">×</span>
        <div class="text-muted small email">
            {{ email }}
        </div>
    </div>
</template>

<script>
    export default {
        name: "User",
        props: ["displayName", "email", "canRemove"]
    }
</script>
