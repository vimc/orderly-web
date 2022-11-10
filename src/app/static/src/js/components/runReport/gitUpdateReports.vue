<template>
    <div>
        <div v-if="reportMetadata && reportMetadata.git_supported" id="git-branch-form-group" class="form-group row">
            <label for="git-branch" class="col-sm-2 col-form-label text-right">Git branch</label>
            <div class="col-sm-6">
                <select id="git-branch" v-model="selectedBranch" class="form-control" @change="changedBranch">
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
                <select id="git-commit" v-model="selectedCommitId" class="form-control" @change="changedCommit">
                    <option v-for="commit in gitCommits" :key="commit.id" :value="commit.id">
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

    export default Vue.extend({
        name: "GitUpdateReports",
        components: {
            ErrorInfo
        },
        props: [
            "reportMetadata",
            "initialBranches",
            "initialBranch",
            "initialCommitId",
            "showAllReports"
        ],
        data: () => {
            return {
                gitRefreshing: false,
                gitBranches: [],
                gitCommits: [],
                selectedBranch: "",
                selectedCommitId: "",
                error: "",
                defaultMessage: "",
                reports: []
            };
        },
        computed: {
            refreshGitText() {
                return this.gitRefreshing ? 'Fetching...' : 'Refresh git'
            },
            showCommits() {
                return this.gitCommits && this.gitCommits.length;
            }
        },
        watch: {
            initialBranches(val) {
                if (val.length > 0) {
                    this.initialise();
                }
            },
            reportMetadata(val) {
                if (val) {
                    this.initialise();
                }
            }
        },
        mounted() {
            if (this.reportMetadata) {
                this.initialise()
            }
        },
        methods: {
            initialise() {
                if (this.reportMetadata?.git_supported) {
                    this.gitBranches = [...this.initialBranches];

                    if (this.initialBranch) {
                        this.selectedBranch = this.initialBranch
                    } else {
                        this.selectedBranch = this.gitBranches.length ? this.gitBranches[0] : "";
                    }
                    this.changedBranch(this.initialCommitId);

                } else {
                    this.updateReports();
                }
            },
            changedBranch(initialCommit = null) {
                this.$emit("branchSelected", this.selectedBranch);
                api.get(`/git/branch/${this.selectedBranch}/commits/`)
                    .then(({data}) => {
                        this.gitCommits = data.data;
                        if (this.gitCommits.length) {
                            if (initialCommit && this.gitCommits.map((c) => c.id).includes(initialCommit)) {
                                this.selectedCommitId = initialCommit;
                            } else {
                                //select the first commit in the branch
                                this.selectedCommitId = this.gitCommits[0].id;
                            }
                            this.changedCommit();
                        }
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred fetching Git commits";
                    });
            },
            changedCommit() {
                console.log("changed commit", this.selectedCommitId)
                this.$emit("commitSelected", this.selectedCommitId);
                this.updateReports();
            },
            updateReports() {
                this.reports = [];
                const showAllParam = this.showAllReports ? "&show_all=true" : "";
                const query = this.reportMetadata?.git_supported ? `?branch=${this.selectedBranch}&commit=${this.selectedCommitId}${showAllParam}` : '';
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
                        console.log("finished refreshing")
                        this.gitRefreshing = false;
                        this.gitBranches = data.data.map(branch => branch.name);

                        this.gitCommits = [];
                        this.reports = [];
                        this.selectedBranch = this.gitBranches.length ? this.gitBranches[0] : [];
                        this.selectedCommitId = "";
                        this.changedBranch();
                    })
                    .catch((error) => {
                        console.log("error refreshing")
                        this.gitRefreshing = false;
                        this.error = error;
                        this.defaultMessage = "An error occurred refreshing Git";
                    });
            },
        }
    });
</script>
