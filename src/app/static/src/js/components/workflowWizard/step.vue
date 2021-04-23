<template>
    <div v-if="active">
        <div>
            <slot></slot>
        </div>
        <div v-if="hasVisibility.cancel" class="pt-4">
            <button type="button" class="btn btn-sm btn-secondary disabled"
                    @click="jump('cancel')">Cancel
            </button>
            <button v-if="hasVisibility.back & !hasVisibility.rerun" type="button" class="btn btn-sm btn-primary"
                    @click="jump('back')">Back
            </button>
            <button v-if="hasVisibility.next" type="button" class="btn btn-sm btn-success"
                    @click="jump('next')">Next
            </button>
            <button v-if="hasVisibility.run" type="button" class="btn btn-sm btn-success"
                    @click="jump('run')">Run workflow
            </button>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue"

interface Props {
    hasVisibility: {}
    active: boolean
}

interface Methods {
    jump: (action: string) => void
}

export default Vue.extend<unknown, Methods, unknown, Props>({
    name: "step",
    props: {
        hasVisibility: {},
        active: {
            type: Boolean,
            required: true
        }
    },
    methods: {
        jump: function (action) {
            this.$emit("jump", action)
        }
    }
})
</script>