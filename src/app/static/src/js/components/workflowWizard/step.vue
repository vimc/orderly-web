<template>
    <div v-if="active && hasValidComponent">
        <div>
            <slot></slot>
        </div>
        <div class="pt-4">
            <button id="cancel-workflow" type="button" class="btn btn-sm btn-secondary"
                    @click="cancel">Cancel
            </button>
            <button id="previous-workflow" v-if="hasVisibility.back" type="button"
                    :class="!enabled.back ? 'disabled' : ''"
                    class="btn btn-sm btn-primary"
                    @click="back">Back
            </button>
            <button id="next-workflow" type="button" class="btn btn-sm btn-success"
                    :class="!enabled.next ? 'disabled' : ''"
                    @click="next"> {{ hasVisibility.next ? "Next" : "Run workflow" }}
            </button>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue"

interface Props {
    hasVisibility: {}
    active: boolean
    enabled: {}
}

interface Methods {
    next: () => void
    back: () => void
    cancel: () => void
}

interface Computed {
    hasValidComponent: boolean
}

export default Vue.extend<unknown, Methods, Computed, Props>({
    name: "step",
    props: {
        hasVisibility: {},
        active: {
            type: Boolean,
            required: true
        },
        enabled: {}
    },
    computed: {
        hasValidComponent() {
            return !!this.$slots.default
        }
    },
    methods: {
        next: function () {
            this.$emit("next")
        },
        back: function () {
            this.$emit("back")
        },
        cancel: function () {
            this.$emit("cancel")
        }
    }
})
</script>