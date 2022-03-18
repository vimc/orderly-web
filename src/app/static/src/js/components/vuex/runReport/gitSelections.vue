<template>
    <div>
        <div v-if="metadata && metadata.git_supported" id="git-branch-form-group" class="form-group row">
            <label for="git-branch" class="col-sm-2 col-form-label text-right">Git branch</label>
            <div class="col-sm-6">
                <select id="git-branch" v-model="selectedBranch" class="form-control">
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

    interface Computed {
        metadata: RunReportMetadataDependency
        gitBranches: string[]
        gitCommits: GitCommit[]
        selectedCommit: string
    }

    interface Methods {
        selectBranch: (branch: string) => void
    }

    export default Vue.extend<EmptyObject, Methods, Computed, EmptyObject>({
        name: "GitSelections",
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
            selectBranch: mapActionByName("git", GitAction.SelectBranch)
        }
    });
</script>
