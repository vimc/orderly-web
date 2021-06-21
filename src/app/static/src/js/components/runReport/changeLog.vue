<template>
    <div v-if="showChangeMessage">
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
                    <option v-for="option in reportMetadata.changelog_types" :value="option">
                        {{ option }}
                    </option>
                </select>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";
    import {ChangelogStyle, ReportMetadata} from "../../utils/types";

    interface Props {
        showChangeMessage: boolean
        reportMetadata: ReportMetadata[] | null
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
            showChangeMessage: {
                required: true,
                type: Boolean
            },
            changelogStyle: Object,
            reportMetadata: null
        },
        methods: {
            handleChangeLogType: function () {
                this.$emit("changelogType", this.changeLogTypeValue)
            },
            handleChangeLogMessage: function () {
                this.$emit("changelogMessage", this.changeLogMessageValue)
            }
        }
    })
</script>
