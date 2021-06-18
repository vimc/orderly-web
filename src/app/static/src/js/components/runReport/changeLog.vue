<template>
    <div>
        <div v-if="showChangelog">
            <div v-if="showChangeMessage" id="changelog-message" class="form-group row">
                <label for="changelogMessage" class="col-sm-2 col-form-label text-right">Changelog Message</label>
                <div class="col-sm-6">
                        <textarea class="form-control" id="changelogMessage" v-model="changeLogMessageValue"
                                  rows="2"></textarea>
                </div>
            </div>
            <div id="changelog-type" class="form-group row">
                <label for="changelogType" class="col-sm-2 col-form-label text-right">Changelog Type</label>
                <div class="col-sm-6">
                    <select class="form-control" id="changelogType" v-model="changeLogTypeValue">
                        <option v-for="option in reportMetadata.changelog_types" :value="option">
                            {{ option }}
                        </option>
                    </select>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue";
import {RunMetadata} from "../../utils/types";

interface Props {
    showChangelog: boolean
    showChangeMessage: boolean
    changeLogMessageValue: string
    changeLogTypeValue: string
    reportMetadata: RunMetadata[] | null
}

export default Vue.extend<unknown, unknown, unknown, Props>({
    name: "changeLog",
    props: {
        showChangelog: {
            required: true,
            type: Boolean
        },
        showChangeMessage: {
            required: true,
            type: Boolean
        },
        changeLogMessageValue: {
            required: true,
            type: String
        },
        changeLogTypeValue: {
            required: true,
            type: String
        },
        reportMetadata: null
    },
    methods: {

    }
})
</script>
