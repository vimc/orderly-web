<template>
    <div>
        <form class="mt-3">
            <div v-if="metadata.git_supported">
                <div id="git-branch-form-group" class="form-group row">
                    <label for="git-branch" class="col-sm-2 col-form-label text-right">Git branch</label>
                    <div class="col-sm-6">
                        <select class="form-control" id="git-branch" v-model="selectedBranch" @change="changedBranch">
                            <option disabled selected value=""> -- Select a branch -- </option>
                            <option v-for="branch in gitBranches" :value="branch">{{branch}}</option>
                        </select>
                    </div>
                </div>
                <div v-if="showCommits" id="git-commit-form-group" class="form-group row">
                    <label for="git-commit" class="col-sm-2 col-form-label text-right">Git commit</label>
                    <div class="col-sm-6">
                        <select class="form-control" id="git-commit" v-model="selectedCommitId">
                            <option v-for="commit in gitCommits" :value="commit.id">
                                {{commit.id}} ({{commit.date_time}})
                            </option>
                        </select>
                    </div>
                </div>
            </div>
         </form>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import ErrorInfo from "../errorInfo";
    export default {
        name: "runReport",
        props: ['metadata', 'gitBranches'],
        components: {ErrorInfo},
        data: () => {
            return {
                gitCommits: [],
                selectedBranch: "",
                selectedCommitId: "",
                error: "",
                defaultMessage: ""
            }
        },
        computed: {
            showCommits: function () {
                return this.gitCommits && this.gitCommits.length;
            }
        },
        methods: {
            changedBranch: function() {
                if (this.selectedBranch) {
                     api.get(`/git/branch/${this.selectedBranch}/commits/`)
                         .then(({data}) => {
                             this.gitCommits = data.data;
                             if (this.gitCommits.length) {
                                 this.selectedCommitId = this.gitCommits[0].id;
                             }
                             this.error = "";
                             this.defaultMessage = "";
                         })
                         .catch((error) => {
                             this.error = error;
                             this.defaultMessage = "An error occurred fetching Git commits";
                         });
                 }
            }
        }
    }
</script>
