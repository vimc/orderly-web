<template>
    <div>
        <h2 id="create-workflow-header">Run workflow</h2>
        <div class="pt-2">
            <p>Either:</p>
            <button id="create-workflow" @click="create()" type="button" class="btn btn-success">
                Create a blank workflow
            </button>
        </div>
        <div id="report-list" class="pt-4">
            <p>Or re-use an existing workflow:</p>
            <!-- use vue-select -->
        </div>
        <div class="pt-4">
            <button id="rerun" @click="rerun()" type="button" class="btn btn-success">Re-run workflow</button>
            <button id="clone" @click="clone()" type="button" class="btn btn-success">Clone workflow</button>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue"

interface Methods {
    create: () => void
    clone: () => void
    rerun: () => void
}

export default Vue.extend<unknown, Methods, unknown, unknown>({
    name: "runWorkflowCreate",
    methods: {
        create: function () {
            this.$emit("create")
        },
        clone: function () {
            this.$emit("clone")
        },
        rerun: function () {
            this.$emit("rerun")
        }
    }
})
</script>