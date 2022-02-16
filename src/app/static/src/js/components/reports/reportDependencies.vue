<template>
    <div v-if="childDependencies.length || error">
        <hr/>
        <div class="row">
            <div v-if="childDependencies.length">
                <h4>Dependencies</h4>
                <div class="col-12">
                    <report-dependency-list :dependency-list="childDependencies"></report-dependency-list>
                </div>
            </div>
            <error-info v-if="error" :default-message="defaultMessage" :api-error="error"></error-info>
        </div>
    </div>
</template>

<script lang="ts">
    import {api} from "../../utils/api";
    import {ReportDependencies, ReportDependency, Error} from "../../utils/types";
    import Vue from "vue";
    import ReportDependencyList from "./reportDependencyList.vue";
    import ErrorInfo from "../errorInfo.vue";

    interface Data {
        dependencies: ReportDependencies | null,
        error: Error | null,
        defaultMessage: string | null
    }

    interface Computed {
        childDependencies: ReportDependency[]
    }

    export default Vue.extend<Data, Record<string, never>, Computed, Record<string, never>>({
        name: "ReportDependencies",
        components: {
            ReportDependencyList,
            ErrorInfo
        },
        props: ['report'],
        data: () => {
            return {
                dependencies: null,
                error: null,
                defaultMessage: null
            }
        },
        computed: {
            childDependencies() {
                //The top level 'dependency_tree' value always has the report in question as the root item, but we don't
                // want to display this report as its own dependency so we start from the next level down
                return this.dependencies ? this.dependencies.dependency_tree.dependencies : [];
            }
        },
        mounted() {
            const params = {id: this.report.id, direction: "upstream"};
            api.get(`/report/${this.report.name}/dependencies/`, {params})
                .then(({data}) => {
                    this.dependencies = data.data as ReportDependencies;
                })
                .catch((error) => {
                    this.defaultMessage = `Could not load report dependencies`;
                    this.error = error;
                });
        }
    })
</script>
