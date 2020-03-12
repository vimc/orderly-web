<template>
    <div>
        <span v-for="tag in value">{{tag}}</span>
    </div>
</template>

<script>
    import Vue from "vue";

    export default Vue.extend({
        props: ["value", "editable"]
    });
</script>
