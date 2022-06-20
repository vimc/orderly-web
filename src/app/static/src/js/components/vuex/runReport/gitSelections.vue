<template>
    <div>
        <div v-if="metadata && metadata.git_supported" id="git-branch-form-group" class="form-group row">
            <label for="git-branch" class="col-sm-2 col-form-label text-right">Git branch</label>
            <div class="col-sm-6">
                <select id="git-branch" :value="selectedBranch" class="form-control" @change="selectBranch($event.target.value)">
                    <option v-for="branch in gitBranches" :key="branch" :value="branch">
                        {{ branch }}
                    </option>
                </select>
            </div>
            <button id="git-refresh-btn"
                    class="btn"
                    :disabled="gitRefreshing"
                    type="submit"
                    @click.prevent="refreshGit">
                {{ refreshGitText }}
            </button>
        </div>
        <div v-if="showCommits" id="git-commit-form-group" class="form-group row">
            <label for="git-commit" class="col-sm-2 col-form-label text-right">Git commit</label>
            <div class="col-sm-6">
                <select id="git-commit" :value="selectedCommitId" @change="selectCommit($event.target.value)"  class="form-control">
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
    import {mapActionByName, mapMutationByName} from "../../utils";
    import {GitAction} from "../../../store/git/actions";
    import { GitMutation } from "../../../store/git/mutations";

    interface Computed {
        metadata: RunReportMetadataDependency
        gitBranches: string[]
        gitCommits: GitCommit[]
        selectedBranch: string
        selectedCommitId: string
        showCommits: boolean
        gitRefreshing: boolean
        refreshGitText: string
    }

    interface Methods {
        selectBranch: (branch: string) => void
        selectCommit: (branch: string) => void
        refreshGit: (branch: string) => void
    }

    export default Vue.extend<EmptyObject, Methods, Computed, EmptyObject>({
        name: "GitSelections",
        computed: {
            ...mapState({
                metadata: (state: RunReportRootState) => state.git.metadata,
                gitBranches: (state: RunReportRootState) => state.git.branches,
                selectedBranch: (state: RunReportRootState) => state.git.selectedBranch,
                gitCommits: (state: RunReportRootState) => state.git.commits,
                selectedCommitId: (state: RunReportRootState) => state.git.selectedCommit,
                gitRefreshing: (state: RunReportRootState) => state.git.gitRefreshing
            }),
            showCommits() {
                return !!this.gitCommits?.length;
            },
            refreshGitText() {
                return this.gitRefreshing ? 'Fetching...' : 'Refresh git'
            },
        },
        methods: {
            selectBranch: mapActionByName("git", GitAction.SelectBranch),
            selectCommit: mapMutationByName("git", GitMutation.SelectCommit),
            refreshGit: mapActionByName("git", GitAction.RefreshGit),
        },
    });
</script>
