<template>
    <div id="changelog-container">
        <div id="changelog-message" class="form-group row">
            <label for="changelogMessage"
                   :class="customStyle.label"
                   class="col-form-label">Changelog Message</label>
            <div id="change-message-control" :class="customStyle.control">
                <textarea class="form-control" id="changelogMessage"
                          v-model="changeLogMessageValue"
                          @input="emitChangelog"
                          rows="2">
                </textarea>
            </div>
        </div>
        <div id="changelog-type" class="form-group row">
            <label for="changelogType"
                   :class="customStyle.label"
                   class="col-form-label">Changelog Type</label>
            <div id="change-type-control" :class="customStyle.control">
                <select class="form-control"
                        id="changelogType"
                        v-model="changeLogTypeValue"
                        @change="emitChangelog">
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
    import {ChildCustomStyle} from "../../utils/types";

    interface Props {
        changelogTypeOptions: string[]
        customStyle: ChildCustomStyle
        initialMessage: string
        initialType: string
    }

    interface Data {
        changeLogMessageValue: string
        changeLogTypeValue: string
    }

    interface Methods {
        emitChangelog: () => void
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
            customStyle: {},
            changelogTypeOptions: Array,
            initialMessage: String,
            initialType: String
        },
        methods: {
            emitChangelog() {
                this.$emit("changelog", this.changeLogMessageValue ? {
                    message: this.changeLogMessageValue,
                    type: this.changeLogTypeValue
                } : null);
            }
        },
        mounted() {
            if (this.initialMessage) {
                this.changeLogMessageValue = this.initialMessage;
            }
            if (this.initialType) {
                this.changeLogTypeValue = this.initialType;
            } else if (this.changelogTypeOptions) {
                this.changeLogTypeValue = this.changelogTypeOptions[0];
                this.emitChangelog();
            }
        }
    })
</script>
