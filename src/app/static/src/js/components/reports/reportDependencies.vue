<template>
    <div v-if="childDependencies.length">
        <hr/>
        <div class="row">
            <h4>Dependencies</h4>
            <div class="col-12">
                <report-dependency-list :dependency-list="childDependencies"></report-dependency-list>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import {api} from "../../utils/api";
    import {ReportDependencies, ReportDependency} from "../../types";
    import Vue from "vue";
    import ReportDependencyList from "./reportDependencyList.vue";

    interface Data {
        dependencies: ReportDependencies | null
    }

    interface Computed {
        childDependencies: ReportDependency[]
    }

    export default Vue.extend<Data,{}, Computed, {}>({
        name: "reportDependencies",
        props: ['report'],
        data: () => {
            return {
                dependencies: null
            }
        },
        computed: {
          childDependencies(){
              return this.dependencies ? this.dependencies.dependency_tree.dependencies : [];
          }
        },
        mounted() {
            const params = {id: this.report.id, direction: "upstream"};
            api.get(`/report/${this.report.name}/dependencies/`, {params})
                .then(({data}) => {
                    //this.dependencies = data.data as ReportDependencies
                    this.dependencies = {
                        direction: "upstream",
                        dependency_tree: {
                            name: "this report",
                            id: "1",
                            dependencies: [
                                {name: "a report with no deps", id: "2", dependencies: []},
                                {name: "a report with two deps", id: "3",
                                 dependencies: [
                                     {name: "a report with one dep", id: "4",
                                       dependencies: [
                                           {name: "most nested dep", id: "5", dependencies: []}
                                       ]
                                     },
                                     {name: "another report with no depz", id: "6", dependencies: []}
                                ]}
                            ]
                        }
                    };//end fake data
                });
        },
        components: {
            ReportDependencyList
        }
    })
</script>
