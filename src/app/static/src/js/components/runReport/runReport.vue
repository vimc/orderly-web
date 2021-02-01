<template>
    <div>
        <form class="mt-3">
            <div v-if="metadata.git_supported">
                <div id="git-branch-form-group" class="form-group row">
                    <label for="git-branch" class="col-sm-2 col-form-label text-right">Git branch</label>
                    <div class="col-sm-6">
                        <select class="form-control" id="git-branch" v-model="selectedBranch" @change="changedBranch">
                            <option v-for="branch in gitBranches" :value="branch">{{ branch }}</option>
                        </select>
                    </div>
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
            </div>
            <div v-for="(options, name) in metadata.instances"
                 v-if="metadata.instances_supported && options.length > 1"
                 class="form-group row">
                <label :for="name" class="col-sm-2 col-form-label text-right">Database "{{ name }}"</label>
                <div class="col-sm-6">
                    <select class="form-control" :id="name" v-model="selectedInstances[name]">
                        <option v-for="option in options" :value="option">
                            {{ option }}
                        </option>
                    </select>
                </div>
            </div>
        </form>
        <button @click="refreshGit" id="git-refresh-btn" class="btn mt-2" type="submit" v-if="metadata.git_supported">Refresh git</button>
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
                defaultMessage: "",
                selectedInstances: {}
            }
        },
        computed: {
            showCommits: function () {
                return this.gitCommits && this.gitCommits.length;
            }
        },
        methods: {
            refreshGit: function () {
                api.post('/git/fetch/')
                    .then(() => {
                        this.refreshPage()
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred refreshing Git commits";
                    });
            },
            refreshPage: function () {
                if (this.metadata.git_supported) {
                    this.selectedBranch = this.gitBranches[0];
                    this.changedBranch();
                }
                if (this.metadata.instances_supported) {
                    const instances = this.metadata.instances;
                    for (const key in instances) {
                        if (instances[key].length > 0) {
                            this.selectedInstances[key] = instances[key][0]
                        }
                    }
                }
            },
            changedBranch: function () {
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
        },
        mounted() {
            this.refreshPage()
            // if (this.metadata.git_supported) {
            //     this.selectedBranch = this.gitBranches[0];
            //     this.changedBranch();
            // }
            // if (this.metadata.instances_supported) {
            //     const instances = this.metadata.instances;
            //     for (const key in instances) {
            //         if (instances[key].length > 0) {
            //             this.selectedInstances[key] = instances[key][0]
            //         }
            //     }
            // }
        }
    }
</script>
