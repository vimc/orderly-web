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
    }

    interface Computed {
        metadata: RunReportMetadataDependency
        gitBranches: string[]
        gitCommits: GitCommit[]
        selectedCommit: string
    }

    interface Methods {
        selectBranch: (branch: string) => void
        preSelectBranch: () => void
    }

    export default Vue.extend<Data, Methods, Computed, EmptyObject>({
        name: "GitSelections",
        data(){
            return {
                newBranch: ""
            }
        },
        computed: {
            ...mapState({
                metadata: (state: RunReportRootState) => state.git.metadata,
                gitBranches: (state: RunReportRootState) => state.git.branches,
                selectedBranch: (state: RunReportRootState) => state.git.selectedBranch,
                gitCommits: (state: RunReportRootState) => state.git.commits,
                selectedCommit: (state: RunReportRootState) => state.git.selectedCommit
            })
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
            }
        },
        mounted(){
            this.preSelectBranch()
        }
    });
</script>
