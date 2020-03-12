<template>
    <div v-if="canEdit || allTags.length>0">Tags:
        <span v-for="tag in allTags" class="badge badge-primary mr-1">{{tag}}</span>
        <a v-if="canEdit" href="#" class="small" @click="editTags">
            <edit-icon></edit-icon>
            Edit tags
        </a>

        <div id="edit-tags"
             v-bind:class="['modal-background', {'modal-hide':!showModal}, {'modal-show':showModal}]">
            <div class="modal-main px-3 py-3" style="max-width: 900px">
                <div class="mb-2 font-weight-bold">Edit tags</div>
                <div class="mb-2">
                    <tag-list class="mr-3 tag-list"
                                header="Report Version Tags"
                                description="These tags only apply to this version"
                                :editable="true"
                                v-model="editedVersionTags">
                    </tag-list>
                    <tag-list class="mr-3 tag-list"
                                header="Report Tags"
                                description="Warning: Editing these tags will change them for all versions of this report"
                                :editable="true"
                                v-model="editedReportTags">
                    </tag-list>
                    <tag-list class="tag-list"
                                header="Orderly Tags"
                                description="These are set in Orderly and cannot be changed"
                                :editable="false"
                                v-model="tags.orderly_tags">
                    </tag-list>
                    <div class="clearfix"></div>
                </div>
                <div class="modal-buttons">
                    <button @click="hideModal" id="cancel-edit-btn" class="btn btn-default">Cancel</button>
                    <button @click="saveTags" id="save-tags-btn" class="btn submit mr-3">Save changes</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import Vue from "vue";
    import {api} from "../../utils/api";
    import EditIcon from "./editIcon";
    import TagList from "./tagList";

    export default Vue.extend({
        props: ['report', 'canEdit'],
        data() {
            return {
                tags: {
                    version_tags: [],
                    report_tags: [],
                    orderly_tags: []
                },
                showModal: false,
                editedVersionTags: [],
                editedReportTags: []
            }
        },
        computed: {
            allTags: function() {
                const all =  [...this.tags.version_tags, ...this.tags.report_tags, ...this.tags.orderly_tags];
                return [...new Set(all)].sort();
            }
        },
        methods: {
            editTags: function() {
                this.editedVersionTags = [...this.tags.version_tags];
                this.editedReportTags = [...this.tags.report_tags];
                this.showModal = true;
            },
            hideModal: function() {
                this.showModal = false;
            },
            saveTags: function() {
                this.hideModal();

                //TODOO: here is where we will post changed, and refresh from backend
                alert("Placeholder for saving version tags " + JSON.stringify(editedVersionTags)
                    + " and report tags " + JSON.stringify(editedReportTags));
            }
        },
        mounted() {
            api.get(`/report/${this.report.name}/version/${this.report.id}/tags/`)
                .then(({data}) => {
                    this.tags = data.data;
                })
        },
        components: {
            EditIcon,
            TagList
        }
    });
</script>
