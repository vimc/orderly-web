<template>
    <div>
        <div v-on:click="publish"
             v-bind:class="['toggle', 'btn', {'btn-published':report.published}, {'off':!report.published}]"
             data-toggle="toggle"
             style="width: 109.281px; height: 38px;">
            <div class="toggle-group">
                <label class="btn btn-published toggle-on">
                    Published</label>
                <label class="btn btn-internal toggle-off">Internal</label>
                <span class="toggle-handle btn btn-default">
            </span>
            </div>
        </div>
        <div class="alert alert-danger" v-if="error.length > 0">
            {{error}}
        </div>
    </div>
</template>

<script>
    import axios from "axios";

    export default {
        name: 'publishSwitch',
        props: ['report'],
        data() {
            return {
                error: ""
            }
        },
        methods: {
            publish: function () {
                axios.post(`/v1/reports/${this.report.name}/versions/${this.report.id}/publish/`)
                    .then(() => {
                        this.$emit('toggle');
                        this.error = "";
                    })
                    .catch(() => {
                      //  this.$emit('toggle');
                       // this.error = "Could not toggle status";
                    });
            }
        }
    };

</script>