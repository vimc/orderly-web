<template>
    <div v-if="active && hasValidComponent">
        <div>
            <slot></slot>
        </div>
        <div class="row justify-content-end col-sm-8 pt-3">
            <button id="cancel-workflow" type="button" class="btn btn-sm btn-secondary m-2"
                    @click="cancel">Cancel
            </button>
            <button id="previous-workflow" v-if="buttonOptions.back" type="button"
                    class="btn btn-sm btn-primary m-2"
                    @click="back">Back
            </button>
            <button id="next-workflow" type="button" class="btn btn-sm btn-success m-2"
                    :disabled="handleValid"
                    @click="next">{{ buttonOptions.hasCustomSubmitLabel ? handleToggledLabel : "Next" }}
            </button>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue"

interface Props {
    buttonOptions: Record<string, boolean>
    active: boolean
    valid: boolean
    submitLabel: string | null
}

interface Methods {
    next: () => void
    back: () => void
    cancel: () => void
}

interface Computed {
    hasValidComponent: boolean
    handleToggledLabel: string
    handleValid: boolean
}

export default Vue.extend<unknown, Methods, Computed, Props>({
    name: "step",
    props: {
        buttonOptions: {},
        active: {
            type: Boolean,
            required: true
        },
        valid: {
            type: Boolean,
            required: true
        },
        submitLabel: {
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
        handleToggledLabel() {
            return this.submitLabel ? this.submitLabel : "Submit"
        },
        handleValid() {
            return !this.valid
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