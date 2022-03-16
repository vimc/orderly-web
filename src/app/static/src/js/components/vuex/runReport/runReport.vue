<template>
    <div>
        <h2>Run a report</h2>
        <form class="mt-3">
            <git-selections/>
        </form>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import GitSelections from "./gitSelections.vue"

    export default Vue.extend({
        name: "RunReport",
        components: {
            GitSelections
        }
    })
</script>
