<template>
    <div id="publish-switch">
        <div :class="['toggle', 'btn', {'btn-published':report.published}, {'off':!report.published}]"
             data-toggle="toggle"
             style="width: 109.281px; height: 38px;"
             @click="publish">
            <div class="toggle-group">
                <label class="btn btn-published toggle-on">
                    Published
                </label>
                <label class="btn btn-internal toggle-off">Internal</label>
                <span class="toggle-handle btn btn-default">
                </span>
            </div>
        </div>
        <div v-if="error.length > 0" class="text-danger mt-3">
            {{ error }}
        </div>
    </div>
</template>

<script>
    import {api} from "../../utils/api";

    export default {
        name: 'PublishSwitch',
        props: ['report'],
        data() {
            return {
                error: ""
            }
        },
        methods: {
            publish: function () {
                api.post(`/report/${this.report.name}/version/${this.report.id}/publish/`)
                    .then(() => {
                        this.$emit('toggle');
                        this.error = "";
                    })
                    .catch(() => {
                        this.error = "Error: could not toggle status";
                    });
            }
        }
    };
</script>