<template>
    <div>
        <div v-if="metadata && metadata.git_supported" id="git-branch-form-group" class="form-group row">
            <label for="git-branch" class="col-sm-2 col-form-label text-right">Git branch</label>
            <div class="col-sm-6">
                <select id="git-branch" v-model="newSelectedBranch" class="form-control">
                    <option v-for="branch in gitBranches" :key="branch" :value="branch">
                        {{ branch }}
                    </option>
                </select>
            </div>
        </div>
        <div v-if="showCommits" id="git-commit-form-group" class="form-group row">
            <label for="git-commit" class="col-sm-2 col-form-label text-right">Git commit</label>
            <div class="col-sm-6">
                <select id="git-commit" v-model="newSelectedCommitId" class="form-control">
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
        newSelectedBranch: string
        newSelectedCommitId: string
    }

    interface Computed {
        metadata: RunReportMetadataDependency
        gitBranches: string[]
        gitCommits: GitCommit[]
        selectedCommitId: string
        showCommits: boolean
    }

    interface Methods {
        selectBranch: (branch: string) => void
        selectCommit: (branch: string) => void
        preSelectBranch: () => void
        preSelectCommit: () => void
    }

    export default Vue.extend<Data, Methods, Computed, EmptyObject>({
        name: "GitSelections",
        data(){
            return {
                newSelectedBranch: "",
                newSelectedCommitId: ""
            }
        },
        computed: {
            ...mapState({
                metadata: (state: RunReportRootState) => state.git.metadata,
                gitBranches: (state: RunReportRootState) => state.git.branches,
                selectedBranch: (state: RunReportRootState) => state.git.selectedBranch,
                gitCommits: (state: RunReportRootState) => state.git.commits,
                selectedCommitId: (state: RunReportRootState) => state.git.selectedCommit
            }),
            showCommits() {
                return !!this.gitCommits?.length;
            }
        },
        methods: {
            selectBranch: mapActionByName("git", GitAction.SelectBranch),
            selectCommit: mapActionByName("git", GitAction.SelectCommit),
            preSelectBranch() {
                const selectedBranch = this.selectedBranch
                if (selectedBranch && this.gitBranches.some(branch => branch === selectedBranch)) {
                    this.newSelectedBranch = selectedBranch;
                } else if (this.gitBranches.length) {
                    this.newSelectedBranch = this.gitBranches[0]
                }
            },
            preSelectCommit() {
                const selectedCommitId = this.selectedCommitId
                if (selectedCommitId && this.gitCommits.some(commit => commit.id === selectedCommitId)) {
                    this.newSelectedCommitId = selectedCommitId;
                } else if (this.gitCommits.length && this.gitCommits[0]?.id) {
                    this.newSelectedCommitId = this.gitCommits[0].id
                }
            },
        },
        watch: {
            newSelectedBranch(){
                this.selectBranch(this.newSelectedBranch)
            },
            newSelectedCommitId(){
                this.selectCommit(this.newSelectedCommitId)
            },
            gitBranches(){
                this.preSelectBranch()
            },
            gitCommits(){
                this.preSelectCommit()
            }
        },
        mounted(){
            this.preSelectBranch()
        }
    });
</script>
