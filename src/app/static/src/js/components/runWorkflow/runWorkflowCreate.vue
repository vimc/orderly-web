<template>
    <div>
        <h2>Run workflow</h2>
        <div class="pt-2">
            <p>Either:</p>
            <button @click="jump('create')" type="button" class="btn btn-success">
                Create a blank workflow
            </button>
        </div>
        <div class="pt-4">
            <p>Or re-use an existing workflow:</p>
            <!-- use vue-select -->
            <v-select :options="['FirstSelectedValue', 'SecondSelectedValue']"></v-select>
        </div>
        <div class="pt-4">
            <button @click="jump('rerun')" type="button" class="btn btn-success">Re-run workflow</button>
            <button @click="jump('clone')" type="button" class="btn btn-success">Clone workflow</button>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue"

interface Methods {
    jump: (action: string) => void
}

export default Vue.extend<unknown, Methods, unknown, unknown>({
    name: "runWorkflowCreate",
    methods: {
        jump: function(action) {
            this.$emit("jump", action)
        }
    }
})
</script>