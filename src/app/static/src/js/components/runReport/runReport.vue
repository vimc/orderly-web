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
                        <select class="form-control" id="git-commit" v-model="selectedCommit.id" @change="changedCommit">
                            <option v-for="(commit, idx) in gitCommits" :value="commit.id">
                                {{commit.id}}
                            </option>
                        </select>
                    </div>
                </div>
            </div>
         </form>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    export default {
        name: "runReport",
        props: ['metadata', 'gitBranches'],
        data: () => {
            return {
                gitCommits: [],
                selectedBranch: "",
                selectedCommit: ""
            }
        },
        computed: {
            showCommits: function() {
                return this.gitCommits && this.gitCommits.length;
            },
           changedBranch: function() {
                if (this.selectedBranch) {
                    api.get(`/git/branch/${this.selectedBranch}/commits/`)
                        .then(({data}) => {
                            this.gitCommits = data.data;
                            if (this.gitCommits.length) {
                                this.selectedCommit = this.gitCommits[0];
                            }
                        })
                }
            },
            changedCommit: function() {
                //TODO
            }
        }
    }
</script>
