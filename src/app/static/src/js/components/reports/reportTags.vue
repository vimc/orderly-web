<template>
    <div>
        <div v-if="allTags.length>0">Tags:
            <span v-for="tag in allTags" class="badge badge-primary mr-1">{{tag}}</span>
        </div>
    </div>
</template>

<script>
    import Vue from "vue";
    import {api} from "../../utils/api";

    export default Vue.extend({
        name: "reportTags",
        props: ['report', 'canEdit'],
        data() {
            return {
                tags: {
                    version_tags: [],
                    report_tags: [],
                    orderly_tags: []
                }
            }
        },
        computed: {
            allTags: function() {
                const all =  [...this.tags.version_tags, ...this.tags.report_tags, ...this.tags.orderly_tags];
                return [...new Set(all)].sort();
            }
        },
        mounted() {
            api.get(`/report/${report.name}/version/${report.id}/tags/`)
                .then(({data}) => {
                    this.tags = data.data;
                })
        }
    });
</script>
