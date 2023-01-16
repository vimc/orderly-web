<template>
    <div v-if="error" class="pt-1 small text-danger">
        <div class="error-message">{{ message }}</div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";
    import {Error} from "../utils/types";

    interface Props {
        error: Error
    }

    interface Computed {
        message: string
    }

    export default Vue.extend<unknown, unknown, Computed, Props>({
        props: {
            "error": Object
        },
        computed: {
            message: function () {
                return this.error.detail ? this.error.detail : this.error.error
            }
        }
    });
</script>
