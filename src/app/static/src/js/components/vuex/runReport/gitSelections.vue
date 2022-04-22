<template>
    <div>
        <div v-if="metadata && metadata.git_supported" id="git-branch-form-group" class="form-group row">
            <label for="git-branch" class="col-sm-2 col-form-label text-right">Git branch</label>
            <div class="col-sm-6">
                <select id="git-branch" v-model="newBranch" class="form-control">
                    <option v-for="branch in gitBranches" :key="branch" :value="branch">
                        {{ branch }}
                    </option>
                </select>
            </div>
        </div>
        <div v-if="showCommits" id="git-commit-form-group" class="form-group row">
            <label for="git-commit" class="col-sm-2 col-form-label text-right">Git commit</label>
            <div class="col-sm-6">
                <select id="git-commit" v-model="selectedCommitId" class="form-control" @change="changedCommit">
                    <option v-for="commit in gitCommits" :key="commit.id" :value="commit.id">
                        {{ commit.id }} ({{ commit.date_time }})
                    </option>
                </select>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";
    import {mapState} from "vuex";
    import {RunReportRootState} from "../../../store/runReport/store";
    import {EmptyObject, GitCommit, RunReportMetadataDependency} from "../../../utils/types";
    import {mapActionByName} from "../../utils";
    import {GitAction} from "../../../store/git/actions";
    import {GitState} from "../../../store/git/git";
    import {namespace} from "../../../store/runReport/store";

    interface Data {
        newBranch: string
        selectedCommitId: string
    }

    interface Computed {
        metadata: RunReportMetadataDependency
        gitBranches: string[]
        gitCommits: GitCommit[]
        selectedCommit: string
        showCommits: boolean
    }

    interface Methods {
        selectBranch: (branch: string) => void
        preSelectBranch: () => void
        changedCommit: () => void
    }

    export default Vue.extend<Data, Methods, Computed, EmptyObject>({
        name: "GitSelections",
        data(){
            return {
                newBranch: "",
                selectedCommitId: ""
            }
        },
        computed: {
            ...mapState({
                metadata: (state: RunReportRootState) => state.git.metadata,
                gitBranches: (state: RunReportRootState) => state.git.branches,
                selectedBranch: (state: RunReportRootState) => state.git.selectedBranch,
                gitCommits: (state: RunReportRootState) => state.git.commits,
                selectedCommit: (state: RunReportRootState) => state.git.selectedCommit
            }),
            showCommits() {
                return !!this.gitCommits?.length;
            }
        },
        methods: {
            selectBranch: mapActionByName("git", GitAction.SelectBranch),
            preSelectBranch() {
                const selectedBranch = this.selectedBranch
                if (selectedBranch && this.gitBranches.some(branch => branch === selectedBranch)) {
                    this.newBranch = selectedBranch;
                } else if (this.gitBranches.length) {
                    this.newBranch = this.gitBranches[0]
                }
            },
            changedCommit(){
                console.log("changed commit")
            }
        },
        watch: {
            newBranch(){
                console.log('newBranch', this.newBranch)
                console.log('metadata', this.metadata)
                if (this.newBranch !== this.selectedBranch){
                    this.selectBranch(this.newBranch)
                }
            },
            gitBranches(){
                this.preSelectBranch()
            },
            gitCommits(){
                console.log('gitCommits', this.gitCommits)

            },
            selectedCommitId(){
                console.log('selectedCommitId', this.selectedCommitId)

            }
        },
        mounted(){
            this.preSelectBranch()
        }
    });
</script>
