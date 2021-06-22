<template>
    <div id="changelog-container" v-if="showChangelog">
        <div id="changelog-message" class="form-group row">
            <label for="changelogMessage"
                   :class="`col-sm-${changelogStyle.label.size} ${changelogStyle.label.justify}`"
                   class="col-form-label">Changelog Message</label>
            <div :class="`col-sm-${changelogStyle.control.size}`">
                        <textarea class="form-control" id="changelogMessage"
                                  v-model="changeLogMessageValue"
                                  @input="handleChangeLogMessage"
                                  rows="2">
                        </textarea>
            </div>
        </div>
        <div id="changelog-type" class="form-group row">
            <label for="changelogType"
                   :class="`col-sm-${changelogStyle.label.size} ${changelogStyle.label.justify}`"
                   class="col-form-label">Changelog Type</label>
            <div :class="`col-sm-${changelogStyle.control.size}`">
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
    import {ChangelogStyle} from "../../utils/types";

    interface Props {
        showChangelog: boolean
        changelogTypeOptions: string[]
        changelogStyle: ChangelogStyle
    }

    interface Data {
        changeLogMessageValue: string
        changeLogTypeValue: string
    }

    interface Methods {
        handleChangeLogMessage: () => void
        handleChangeLogType: () => void
    }

    export default Vue.extend<Data, Methods, unknown, Props>({
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
            changelogStyle: Object,
            changelogTypeOptions: []
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
            if(this.changelogTypeOptions) {
                this.changeLogTypeValue = this.changelogTypeOptions[0]
                this.$emit("changelogType", this.changeLogTypeValue)
            }
        }
    })
</script>
