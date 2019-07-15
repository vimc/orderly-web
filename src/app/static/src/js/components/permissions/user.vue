<template>
    <div class="removable-user">
        <span class="display-name">{{displayName}}</span>
        <span v-if="canRemove"
              v-on:click="remove"
              class="remove-user d-inline-block ml-2 large">×</span>
        <div class="text-muted small email">{{email}}</div>
    </div>
</template>

<script>
    export default {
        name: "user",
        props: ["displayName", "email", "canRemove"],
        methods: {
            remove() {
                this.$emit('remove', this.email)
            }
        }
    }
</script>
