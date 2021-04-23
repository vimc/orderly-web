<template>
    <div v-if="active">
        <div>
            <slot></slot>
        </div>
        <div v-if="number !== 1" class="pt-4">
            <button type="button"
                    class="btn btn-sm btn-secondary disabled"
                    name="cancel" @click="jump('cancel')">Cancel
            </button>
            <button v-if="number > 2" type="button" class="btn btn-sm btn-primary"
                    name="back" @click="jump('back')">Back
            </button>
            <button v-if="number !== 4" type="button" class="btn btn-sm btn-success"
                    name="next" @click="jump('next')">Next
            </button>
            <button v-if="number === 4" type="button" class="btn btn-sm btn-success"
                    name="run" @click="jump('run')">Run workflow
            </button>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue"

interface Props {
    name: string | null
    active: boolean
    number: number
}

interface Methods {
    jump: (action: string) => void
}

export default Vue.extend<unknown, Methods, unknown, Props>({
    name: "step",
    props: {
        name: null,
        number: {
            type: Number,
            required: true
        },
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