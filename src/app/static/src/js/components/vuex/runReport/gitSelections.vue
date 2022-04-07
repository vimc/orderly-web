<template>
    <div>
        <div v-if="metadata && metadata.git_supported" id="git-branch-form-group" class="form-group row">
            <label for="git-branch" class="col-sm-2 col-form-label text-right">Git branch</label>
            <div class="col-sm-6">
                <select id="git-branch" class="form-control">
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
    import {EmptyObject, RunReportMetadataDependency} from "../../../utils/types";
    import {GitState} from "../../../store/git/git";
    import {namespace} from "../../../store/runReport/store";

    interface Computed {
        metadata: RunReportMetadataDependency
        gitBranches: string[]
    }

    export default Vue.extend<EmptyObject, EmptyObject, Computed, EmptyObject>({
        name: "GitSelections",
        computed: {
            ...mapState(namespace.git, {
                metadata: (state: GitState) => state.metadata,
                gitBranches: (state: GitState) => state.branches
            })
        }
    });
</script>
