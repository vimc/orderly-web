<template>
    <div id="confirm-cancel-container"
         :class="['modal-background', {'modal-hide':!showModal}, {'modal-show':showModal}]">
        <div class="modal-main px-3 py-3">
            <div id="cancel-heading" class="mb-2 font-weight-bold">
                Confirm cancellation
            </div>
            <div id="cancel-text" class="mb-2">
                Are you sure you want to cancel?
            </div>
            <div class="modal-buttons">
                <button id="confirm-cancel-btn" class="btn submit mr-3" @click="cancel">
                    Yes
                </button>
                <button id="confirm-abort-cancel-btn" class="btn btn-default" @click="abortCancel">
                    No
                </button>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"

    interface Props {
        showModal: boolean
    }

    interface Methods {
        cancel: () => void
        abortCancel: () => void
    }

    export default Vue.extend<unknown, Methods, unknown, Props>({
        name: "CancelDialog",
        props: {
            showModal: {
                type: Boolean,
                required: true
            }
        },
        methods: {
            cancel: function () {
                this.$emit("cancel")
            },
            abortCancel: function () {
                this.$emit("abortCancel")
            }
        }
    })
</script>