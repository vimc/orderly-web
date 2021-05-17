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
                    class="btn btn-sm btn-primary"
                    @click="back">Back
            </button>
            <button id="next-workflow" type="button" class="btn btn-sm btn-success"
                    :class="{disabled: !valid}"
                    @click="next"> {{ hasVisibility.next ? "Next" : handleToggledButton}}
            </button>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue"

interface Props {
    hasVisibility: {}
    active: boolean
    valid: boolean
    toggleNext: string | null
}

interface Methods {
    next: () => void
    back: () => void
    cancel: () => void
}

interface Computed {
    hasValidComponent: boolean
    handleToggledButton: string
}

export default Vue.extend<unknown, Methods, Computed, Props>({
    name: "step",
    props: {
        hasVisibility: {},
        active: {
            type: Boolean,
            required: true
        },
        valid: {
            type: Boolean,
            required: true
        },
        toggleNext: {
            type: String,
            required: false
        }
    },
    computed: {
        hasValidComponent() {
            /**
             * A defensive approach to ensuring components are provided in the slot before displaying,
             * the slot has "default" name as I didnt name it specifically.
             */
            return !!this.$slots.default
        },
        handleToggledButton() {
            return this.toggleNext ? this.toggleNext : "Submit"
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