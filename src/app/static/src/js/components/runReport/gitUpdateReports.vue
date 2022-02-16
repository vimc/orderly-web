<template>
    <div>
        <div v-if="metadata && metadata.git_supported" id="git-branch-form-group" class="form-group row">
            <label for="git-branch" class="col-sm-2 col-form-label text-right">Git branch</label>
            <div class="col-sm-6">
                <select class="form-control" id="git-branch" v-model="selectedBranch">
                    <option v-for="branch in gitBranches" :value="branch">{{ branch }}</option>
                </select>
            </div>
            <button @click.prevent="refreshGit"
                    id="git-refresh-btn"
                    class="btn"
                    :disabled="gitRefreshing"
                    type="submit">
                {{ refreshGitText }}
            </button>
        </div>
        <div v-if="showCommits" id="git-commit-form-group" class="form-group row">
            <label for="git-commit" class="col-sm-2 col-form-label text-right">Git commit</label>
            <div class="col-sm-6">
                <select class="form-control" id="git-commit" v-model="selectedCommitId">
                    <option v-for="commit in gitCommits" :value="commit.id">
                        {{ commit.id }} ({{ commit.date_time }})
                    </option>
                </select>
            </div>
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";
    import {api} from "../../utils/api";
    import ErrorInfo from "../errorInfo.vue";
    import {mapMutations, mapState} from "vuex";
    import {GitMutation} from "../../store/git/mutations";
    import {RunnerRootState} from "../../utils/types";

    export default Vue.extend({
        name: "gitUpdateReports",
        props: [
            "showAllReports"
        ],
        components: {
            ErrorInfo
        },
        data: () => {
            return {
                gitRefreshing: false,
                gitBranches: [],
                gitCommits: [],
                error: "",
                defaultMessage: "",
                reports: []
            };
        },
        computed: {
            ...mapState({
                initialBranches: (state: RunnerRootState) => state.git.gitBranches,
                metadata: (state: RunnerRootState) => state.git.metadata
            }),
            selectedBranch: {
                get() {
                    return this.$store.state.git.selectedBranch
                },
                set(newVal: string) {
                    this.selectBranch(newVal);
                    this.fetchCommits(newVal);
                }
            },
            selectedCommitId: {
                get() {
                    return this.$store.state.git.selectedCommitId
                },
                set(newVal: string) {
                    this.selectCommit(newVal);
                    if (newVal) {
                        this.fetchReports(this.selectedBranch, newVal);
                    }
                }
            },
            refreshGitText() {
                return this.gitRefreshing ? 'Fetching...' : 'Refresh git'
            },
            showCommits() {
                return this.gitCommits && this.gitCommits.length;
            }
        },
        methods: {
            ...mapMutations({
                selectBranch: `git/${GitMutation.SelectBranch}`,
                selectCommit: `git/${GitMutation.SelectCommitId}`
            }),
            initialise() {
                if (this.metadata && this.metadata.git_supported) {
                    this.gitBranches = [...this.initialBranches];
                }
                this.fetchCommits(this.selectedBranch);
            },
            fetchCommits(branch) {
                api.get(`/git/branch/${branch}/commits/`)
                    .then(({data}) => {
                        this.gitCommits = data.data;
                        if (this.gitCommits.length) {
                            if (this.gitCommits.map((c) => c.id).includes(this.selectedCommitId)) {
                                // the current selected commit is valid so just go ahead and fetch reports
                                this.fetchReports(branch, this.selectedCommitId);
                            } else {
                                // select the first commit in the branch
                                this.selectedCommitId = this.gitCommits[0].id;
                            }
                        }
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred fetching Git commits";
                    });
            },
            fetchReports(branch, commit) {
                this.reports = [];
                const showAllParam = this.showAllReports ? "&show_all=true" : "";
                const query = this.metadata?.git_supported ? `?branch=${branch}&commit=${commit}${showAllParam}` : '';
                api.get(`/reports/runnable/${query}`)
                    .then(({data}) => {
                        this.reports = data.data;
                        this.$emit("reportsUpdate", this.reports);
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred fetching reports";
                    });
            },
            refreshGit: function () {
                this.gitRefreshing = true;
                api.get('/git/fetch/')
                    .then(({data}) => {
                        this.gitRefreshing = false;
                        this.gitBranches = data.data.map(branch => branch.name);

                        this.gitCommits = [];
                        this.reports = [];
                        this.selectedBranch = this.gitBranches.length ? this.gitBranches[0] : [];
                        this.selectedCommitId = "";
                    })
                    .catch((error) => {
                        this.gitRefreshing = false;
                        this.error = error;
                        this.defaultMessage = "An error occurred refreshing Git";
                    });
            },
        },
        mounted() {
            this.initialise();
        },
        watch: {
            initialBranches(val) {
                if (val.length > 0) {
                    this.initialise();
                }
            },
            metadata(val) {
                if (val) {
                    this.initialise();
                }
            }
        }
    });
</script>
