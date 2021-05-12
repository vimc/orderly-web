<template>
    <div v-if="active">
        <div>
            <slot></slot>
        </div>
        <div class="pt-4" v-if="hasValidComponent">
            <button type="button" class="btn btn-sm btn-secondary"
                    @click="cancel">Cancel
            </button>
            <button v-if="hasVisibility.back" type="button"
                    :class="!valid ? 'disabled' : ''"
                    class="btn btn-sm btn-primary"
                    @click="back">Back
            </button>
            <button type="button" class="btn btn-sm btn-success"
                    :class="!valid ? 'disabled' : ''"
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
    valid: boolean
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
        valid: {
            type: Boolean,
            required: true
        }
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