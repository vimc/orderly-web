<template>
    <div v-if="showChangelog">
        <div id="changelog-message" class="form-group row">
            <label for="changelogMessage"
                   :class="displayStyle.label"
                   class="col-form-label">Changelog Message</label>
            <div id="change-message-control" :class="displayStyle.control">
                        <textarea class="form-control" id="changelogMessage"
                                  v-model="changeLogMessageValue"
                                  @input="handleChangeLogMessage"
                                  rows="2">
                        </textarea>
            </div>
        </div>
        <div id="changelog-type" class="form-group row">
            <label for="changelogType"
                   :class="displayStyle.label.toString()"
                   class="col-form-label">Changelog Type</label>
            <div id="change-type-control" :class="displayStyle.control.toString()">
                <select class="form-control"
                        id="changelogType"
                        v-model="changeLogTypeValue"
                        @change="handleChangeLogType">
                    <option v-for="option in changelogTypeOptions" :value="option">
                        {{ option }}
                    </option>
                </select>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";

    interface Props {
        showChangelog: boolean
        changelogTypeOptions: string[]
        changelogStyleReport: boolean
    }

    interface Data {
        changeLogMessageValue: string
        changeLogTypeValue: string
    }

    interface Methods {
        handleChangeLogMessage: () => void
        handleChangeLogType: () => void
    }

    interface Computed {
        displayStyle: object
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "changeLog",
        data(): Data {
            return {
                changeLogMessageValue: "",
                changeLogTypeValue: ""
            }
        },
        props: {
            showChangelog: {
                required: true,
                type: Boolean
            },
            changelogStyleReport: {
                required: false,
                type: Boolean,
                default: false
            },
            changelogTypeOptions: Array
        },
        computed: {
            displayStyle() {
                if (this.changelogStyleReport) {
                    return {label: "col-sm-2 text-right", control: "col-sm-6"}
                }

                return {label: "col-sm-4 text-left", control: "col-sm-4"}
            }
        },
        methods: {
            handleChangeLogType: function () {
                this.$emit("changelogType", this.changeLogTypeValue)
            },
            handleChangeLogMessage: function () {
                this.$emit("changelogMessage", this.changeLogMessageValue)
            }
        },
        mounted() {
            if (this.changelogTypeOptions) {
                this.changeLogTypeValue = this.changelogTypeOptions[0]
                this.$emit("changelogType", this.changeLogTypeValue)
            }
        }
    })
</script>
