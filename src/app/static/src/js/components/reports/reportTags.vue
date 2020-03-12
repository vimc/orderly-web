<template>
    <div v-if="canEdit || allTags.length>0">Tags:
        <span v-for="tag in allTags" class="badge badge-primary mr-1">{{tag}}</span>
        <a v-if="canEdit" href="#" class="small" title="Coming soon!" data-toggle="tooltip">
            <edit-icon></edit-icon>
            Edit tags
        </a>
    </div>
</template>

<script>
    import Vue from "vue";
    import {api} from "../../utils/api";
    import EditIcon from "./editIcon";

    export default Vue.extend({
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
            api.get(`/report/${this.report.name}/version/${this.report.id}/tags/`)
                .then(({data}) => {
                    this.tags = data.data;
                })
        },
        components: {
            EditIcon
        }
    });
</script>
